package impl.servercommunication

import data.LastSteps
import impl.servercommunication.function.{GoodbyeFunction, ReactFunction, WelcomeFunction}
import impl.data.XY


class InputParser(input: String) {

  private val (parsedFunctionName, parsedParams) = parse(input)

  val functionName = parsedFunctionName

  val isWelcomeFunction = functionName == "Welcome"
  val isReactFunction = functionName == "React"
  val isGoodbyeFunction = functionName == "Goodbye"

  val result = prepareResultObject

  def prepareResultObject: Any = {
    if (isWelcomeFunction) {
      new WelcomeFunction(parsedParams.getOrElse("name", ""),
        parsedParams.getOrElse("apocalypse", "0").toInt,
        parsedParams.getOrElse("round", "0").toInt,
        parsedParams.getOrElse("maxSlaves", "0").toInt)
    } else if (isReactFunction) {
      new ReactFunction(parsedParams.getOrElse("generation", "0").toInt,
        parsedParams.getOrElse("name", ""),
        parsedParams.getOrElse("time", "0").toInt,
        parsedParams.getOrElse("view", ""),
        parsedParams.getOrElse("energy", "0").toInt,
        getXYParamOption("master"),
        getXYParamOption("collision"),
        parsedParams.getOrElse("slaves", "0").toInt,
        parsedParams.getOrElse(CustomStatus.ROLE, "master"),
        parsedParams.getOrElse(CustomStatus.MAX_SLAVES, "1000000").toInt,
        parsedParams.getOrElse(CustomStatus.TIME_FROM_CREATION, "0").toInt,
        XY.parse(parsedParams.getOrElse(CustomStatus.DESTINATION, "0:0")),
        parsedParams.getOrElse(CustomStatus.DESTINATION_CHANGE_TIME, "0").toInt,
        LastSteps.parse(parsedParams.getOrElse(CustomStatus.LAST_STEPS, "0:0"))
      )
    } else if (isGoodbyeFunction) {
      new GoodbyeFunction(parsedParams.getOrElse("energy", "0").toInt)
    } else {
      throw new IllegalArgumentException("Unknown method received")
    }
  }

  def getXYParamOption(name: String): Option[XY] = {
    val paramOption = parsedParams.get(name)
    if (paramOption.isDefined) {
      Option(XY.parse(paramOption.get))
    } else {
      None
    }
  }

  /** Got from https://github.com/scalatron/scalatron/blob/master/Scalatron/doc/markdown/Scalatron%20Tutorial.md#bot-5-creating-a-command-parser-function */
  def parse(command: String) = {
    def splitParam(param: String) = {
      val segments = param.split('=')
      if (segments.length != 2)
        throw new IllegalStateException("invalid key/value pair: " + param)
      (segments(0), segments(1))
    }

    val segments = command.split('(')
    if (segments.length != 2)
      throw new IllegalStateException("invalid command: " + command)

    val params = segments(1).dropRight(1).split(',')
    val keyValuePairs = params.map(splitParam).toMap
    (segments(0), keyValuePairs)
  }
}
