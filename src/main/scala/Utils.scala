object Utils {

  val IpPort = """(\d{1,3}(?:\.\d{1,3}){3}):(\d+)""".r

  def waitUntil(condition: => Boolean): Unit = {
    while(!condition) {
      Thread.sleep(250)
    }
  }

}
