package com.biosimilarity.evaluator.importer

import com.biosimilarity.evaluator.distribution.{AccordionConfiguration, DSLCommLinkConfiguration, EvalConfig, EvaluationCommsService}
import com.biosimilarity.evaluator.importer.dtos._
import com.biosimilarity.evaluator.importer.models._
import com.biosimilarity.evaluator.importer.utils.mailinator.Mailinator
import com.biosimilarity.evaluator.spray.{BTCHandler, DownStreamHttpCommsT, EvalHandler}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import scala.collection.JavaConversions._
import scala.util.Random

import scalaj.http.Http

/**
 * Iterates through a sample data file, parses it, and imports the data
 *
 * @note To run the importer, ensure you are using a recent version of Maven.
 * @note First run `mvn clean compile` in the terminal.
 * @note Second run `mvn scala:console`.
 * @note Finally, import this object and execute `Importer.fromFiles()`. You may need to set the file paths in the parameters.
 */
object Importer extends EvalHandler
with EvaluationCommsService
with EvalConfig
with DSLCommLinkConfiguration
with AccordionConfiguration
with DownStreamHttpCommsT
with BTCHandler
with Serializable {

  private val GLOSEVAL_HOST = "http://52.35.10.219:9876/api"
  private val GLOSEVAL_SENDER = "splicious.ftw@gmail.com"
  private val MAILINATOR_KEY = "efa3a1b773db4f0c9492686d24bed415"


  private def glosevalPost(msgType: String, data: RequestContent): String = {
    Http(GLOSEVAL_HOST).postData(write(ApiRequest(msgType, data))).header("Content-Type", "application/json").asString.body
  }

  private def createEmailUser(loginId: String) = s"livelygig_$loginId"

  private def createEmailAddress(loginId: String) = s"${createEmailUser(loginId)}@mailinator.com"

  private def makeAliasURI(alias: String) = s"alias://$alias/alias"

  private def threadSleep = println("Sleeping for 15s"); Thread.sleep(15000L)

  private val sessionsByAgent = scala.collection.mutable.Map[String, InitializeSessionResponse]() // loginId:agentURI
  private val agentsBySession = scala.collection.mutable.Map[String, AgentDesc]() // sessionURI:agent
  private val sessionsById = scala.collection.mutable.Map[String, InitializeSessionResponse]() // sessionURI:agent
  private val aliasesById = scala.collection.mutable.Map[String, String]() // agentId:aliasUri



  def fromFiles(
    dataJsonFile: String = "/Users/justin/Projects/LivelyGig/Product/jvm/src/main/resources/sample-data-demo.json",
    configJsonFile: String = "/Users/justin/Projects/LivelyGig/Product/jvm/src/main/resources/livelygig-system-labels.json"
  ) {
    println("Beginning import procedure")
    //val configJson = scala.io.Source.fromFile(configJsonFile).getLines.map(_.trim).mkString
    //val config = parse(configJson).extract[ConfigDesc]
    val dataJson = scala.io.Source.fromFile(dataJsonFile).getLines.map(_.trim).mkString
    val dataset = parse(dataJson).extract[DataSetDesc]
    val agents = dataset.agents


    println("Importing agents")
    agents.foreach { agent =>
      println(glosevalPost("createUserRequest", CreateUserRequest(createEmailAddress(agent.loginId), agent.pwd, Map("name" -> agent.firstName), true)))
    }
    println("Agents import complete")

    println("Confirming agent emails")
    agents.foreach { agent =>
      try {
        val emailConfirmId = Mailinator.getInboxMessages(MAILINATOR_KEY, createEmailUser(agent.loginId)).toList.filter(m => m.getFrom == GLOSEVAL_SENDER).sortBy(_.getSeconds_ago).head.getId
        Mailinator.getEmail(MAILINATOR_KEY, emailConfirmId).getEmailParts.toList.filter(p => p.getBody.contains("token is")).foreach { part =>
          val token = part.getBody.replace("Your token is: ", "").trim.stripLineEnd
          println(s"Found token for ${agent.loginId}: $token. Confirming....")
          println(glosevalPost("confirmEmailToken", ConfirmEmailRequest(token)))
          threadSleep
        }
      } catch {
        case e: Exception => println(s"Skipping agent ${agent.loginId} because ${e.getMessage}")
      }
    }
    println("Agent emails confirmation complete")

    println("Initializing agent sessions")
    agents.slice(10, agents.size).foreach { agent =>
      val session = parse(glosevalPost("initializeSessionRequest", InitializeSessionRequest(s"agent://email/${createEmailAddress(agent.loginId)}?password=${agent.pwd}"))).extract[ApiResponse[InitializeSessionResponse]].content
      sessionsByAgent.put(agent.loginId, session)
      agentsBySession.put(session.sessionURI, agent)
      sessionsById.put(agent.id, session)
      threadSleep
    }
    println("Agent session initialization complete")

    println("Adding labels for agents")
    agentsBySession.foreach { case (session, agent) =>
      println(glosevalPost("addAliasLabelsRequest", AddAliasLabelsRequest(session, "alias", List(agent.id))))
      aliasesById.put(agent.id, s"alias://${agent.id}/alias")
      threadSleep
    }
    println("Agent labels complete")

    println("Creating connection introductions")
    dataset.cnxns.foreach { connection =>
      val sourceId = connection.src.replace("agent://","")
      val sourceAlias = aliasesById(sourceId)
      val sourceURI = sessionsById(sourceId).sessionURI
      val targetId = connection.trgt.replace("agent://","")
      val targetAlias = aliasesById(targetId)
      println(glosevalPost("beginIntroductionRequest", BeginIntroductionRequest(sourceURI, "alias", Connection(sourceAlias, targetAlias), Connection(targetAlias, sourceAlias))))
      threadSleep
    }
    println("Random introduction connections complete")

    // iterate and create connection confirmations
    /*dataset.cnxns.foreach { connection =>
      val sessionId = connection.src+":::"+connection.trgt
      
      val jConnection: JValue =
        ("content" ->
          ("sessionURI" -> sessionId) ~
          ("alias" -> "someURI") ~ // TODO: this is actually the agent URI that comes back from API
          ("introSessionId" -> "someURI") ~ // TODO: this is the session URI
          ("correlationId" -> "someURI") ~ // TODO: correlationId comes back when calling `beginIntroductionRequest`
          ("accepted" -> true)
        )
      introductionConfirmationRequest(jConnection)

      val jConnection: JValue =
        ("content" ->
          ("sessionURI" -> sessionId) ~
            ("alias" -> "someURI") ~ // TODO: this is actually the agent URI that comes back from API
            ("introSessionId" -> "someURI") ~ // TODO: this is the session URI
            ("correlationId" -> "someURI") ~ // TODO: correlationId comes back when calling `beginIntroductionRequest`
            ("accepted" -> true)
          )
      introductionConfirmationRequest(jConnection)
    }*/

  }

}
