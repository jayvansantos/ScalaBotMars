package impl.reactor.senses

import impl.servercommunication.command.Spawn
import impl.analyser.{PathFinder, ViewAnalyser}
import impl.data.{MiniBotRoles, XY}

/**
 * @author Marcin Pieciukiewicz
 */
class MissileDefence(viewAnalyser: ViewAnalyser, slavesCount: Int, maxSlaves: Int) {


  def calculateCommands(): Option[Spawn] = {

    val targets: List[XY] = viewAnalyser.enemyMiniBots ::: viewAnalyser.enemyBots ::: viewAnalyser.badBeasts
    val myMiniBots: List[XY] = viewAnalyser.myMiniBots

    if (slavesCount < maxSlaves) {
      targets.foreach(target => {
        val (nextStep, pathLength) = PathFinder.findNextStepAndDistance(viewAnalyser, target)

        if (pathLength < 15) {

          val notEnoughMissilesNear = myMiniBots.count(myBotRelativePosition => {
            PathFinder.calculateRequiredSteps(myBotRelativePosition, target) < 10
          }) < 4

          if (notEnoughMissilesNear && targets.size + 5 > myMiniBots.size) {
            return Option(new Spawn(nextStep, "antiMissile", 100, MiniBotRoles.MISSILE))
          }
        }

      })
    }
    None
  }


}
