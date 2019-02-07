// *****************************************************************************
// Projects
// *****************************************************************************

lazy val vitess_init =
  project
    .in(file("."))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.quill,
        library.slick,
        library.mysql,
        library.hikari,
        library.slickHikari,
        library.scalaCheck % Test,
        library.scalaTest  % Test,
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val quill    = "2.5.4"
      val slick    = "3.2.3"
      val mySql    = "5.1.47" //8.0.13"
      val hikariCp = "2.7.9"

      val scalaCheck = "1.14.0"
      val scalaTest  = "3.0.5"

    }
    val quill = "io.getquill" %% "quill"               % Version.quill
    val mysql = "mysql"       % "mysql-connector-java" % Version.mySql

    val slick       = "com.typesafe.slick" %% "slick"          % Version.slick
    val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
    val hikari      = "com.zaxxer"         % "HikariCP"        % Version.hikariCp

    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
commonSettings ++
scalafmtSettings

lazy val commonSettings =
  Seq(
    scalaVersion := "2.12.8",
    organization := "de.exellio",
    organizationName := "ksilin",
    startYear := Some(2019),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding",
      "UTF-8",
      "-Ypartial-unification",
      "-Ywarn-unused-import",
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true,
  )
