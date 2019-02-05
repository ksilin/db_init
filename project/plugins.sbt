addSbtPlugin("com.dwijnand"      % "sbt-dynver"      % "3.1.0")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"    % "1.5.1")

libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.25" // Needed by sbt-git
