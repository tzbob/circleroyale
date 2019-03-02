//resolvers in ThisBuild += "Sonatype OSS Snapshots" at
//  "https://oss.sonatype.org/content/repositories/snapshots"

organization in ThisBuild := "be.tzbob"
scalaVersion in ThisBuild := "2.12.4"
version in ThisBuild := "0.0.1-SNAPSHOT"

scalacOptions in ThisBuild ++= Seq(
  "-encoding",
  "UTF-8",
  "-feature",
  "-deprecation",
  // "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ypartial-unification",
  "-language:higherKinds",
  "-language:implicitConversions"
)

lazy val example = crossProject
  .in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "kooi-examples",
    buildInfoKeys := Seq[BuildInfoKey](name),
    buildInfoPackage := "be.tzbob.examples",
    addCompilerPlugin(
      "org.scalamacros"                % "paradise"        % "2.1.0" cross CrossVersion.full),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
    libraryDependencies ++= Seq(
      "be.tzbob"      %%% "gavial"      % "0.4.0-SNAPSHOT",
      "org.scalatest" %%% "scalatest"   % "3.0.5" % "test",
      "org.typelevel" %%% "cats-effect" % "1.0.0"
    ),
    useYarn := true,
    scalaJSUseMainModuleInitializer := true,
    emitSourceMaps := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    //mainClass in Compile := Some("be.tzbob.circleroyale._1Client")
//      mainClass in Compile := Some("be.tzbob.circleroyale._3SessionServerTick")
//    mainClass in Compile := Some("be.tzbob.circleroyale._4App")
            mainClass in Compile := Some("be.tzbob.circleroyale._5Chat")
//      mainClass in Compile := Some("be.tzbob.circleroyale.CircleRoyale")
//      mainClass in Compile := Some("be.tzbob.examples.Counters")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "0.5.1",
      "org.tpolecat" %% "doobie-h2"   % "0.5.1"
    )
  )

// Needed, so sbt finds the projects
lazy val exampleJS = example.js
  .settings(
    WebKeys.packagePrefix in Assets := "content/"
  )
  .enablePlugins(ScalaJSBundlerPlugin, ScalaJSWeb)

lazy val exampleJVM = example.jvm
  .settings(
    scalaJSProjects := Seq(exampleJS),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    managedClasspath in Runtime += (packageBin in Assets).value
  )
  .enablePlugins(WebScalaJSBundlerPlugin)

onLoad in Global ~= (_ andThen ("project exampleJVM" :: _))
