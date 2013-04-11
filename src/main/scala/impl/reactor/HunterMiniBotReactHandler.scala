package impl.reactor

import impl.analyser.{DirectionAdvisor, ViewAnalyser}
import impl.servercommunication.command._
import impl.data.{Step, MiniBotRoles, DirectionPreferences, XY}
import impl.servercommunication.function.ReactFunction
import senses._
import impl.configuration.Parameters
import impl.servercommunication.CustomStatus

/**
 * @author Marcin Pieciukiewicz
 */
class HunterMiniBotReactHandler(reactFunction: ReactFunction, viewAnalyser: ViewAnalyser) {

  private val timeFromCreation = reactFunction.timeFromCreation

  def respond(): Commands = {

    if (viewAnalyser.nearestEnemyDistance < 2 || viewAnalyser.enemiesCount > 0 && math.random < Parameters.PROBABILITY_TO_CHANGE_INTO_MISSILE && reactFunction.timeFromCreation > 2) {
      new SetCommand(Map(CustomStatus.ROLE -> MiniBotRoles.MISSILE)) :: new MissileMiniBotReactHandler(reactFunction, viewAnalyser).respond()
    } else {

      var multiplePreferences = List[DirectionPreferences]()

      multiplePreferences ::= new Loner(viewAnalyser).calculatePreferences() * Parameters.HUNTER_LONER
      multiplePreferences ::= new Hunger(viewAnalyser).calculatePreferences() * Parameters.HUNTER_HUNGER
      multiplePreferences ::= new Fear(viewAnalyser).calculatePreferences() * Parameters.HUNTER_FEAR
      multiplePreferences ::= new CabinFever(viewAnalyser).calculatePreferences() * Parameters.HUNTER_CABIN_FEVER

      if (timeFromCreation >= 20) {
        multiplePreferences ::= new GoHome(viewAnalyser, reactFunction).calculatePreferences() * Parameters.HUNTER_GO_HOME
      }

      val preferences = multiplePreferences.foldLeft(new DirectionPreferences)(_ + _)


      val step: Step = DirectionAdvisor.findBestMoveFromPreferences(preferences, viewAnalyser, true)


      var commands = new Commands(new Move(step))
      commands ::= new SetCommand(Map(CustomStatus.TIME_FROM_CREATION -> (timeFromCreation + 1).toString))
      if (timeFromCreation > 10 && reactFunction.energy > 150 && (viewAnalyser.myMiniBots.size < 10 || viewAnalyser.enemyMiniBots.size > 0 || viewAnalyser.enemyBots.size > 0)) {

        val spawnStep = preferences.findBestStep().rotate(2)

        commands ::= new Spawn(spawnStep, "hunter", 100, MiniBotRoles.HUNTER)
      }
      commands
    }


  }


}
