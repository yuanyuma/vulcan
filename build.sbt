import ReleaseTransformations._

val avroVersion = "1.9.0"

val catsVersion = "1.6.1"

val magnoliaVersion = "0.10.0"

val shapelessVersion = "2.3.3"

lazy val root = project
  .in(file("."))
  .settings(
    dependencySettings,
    noPublishSettings,
    mimaSettings,
    scalaSettings,
    testSettings,
    console := (console in (core, Compile)).value,
    console in Test := (console in (core, Test)).value
  )
  .aggregate(core)

lazy val core = project
  .in(file("modules/core"))
  .settings(
    moduleName := "vulcan",
    name := moduleName.value,
    dependencySettings,
    publishSettings,
    mimaSettings,
    scalaSettings,
    testSettings
  )

lazy val docs = project
  .in(file("docs"))
  .settings(
    moduleName := "vulcan-docs",
    name := moduleName.value,
    dependencySettings,
    noPublishSettings,
    scalaSettings,
    mdocSettings,
    buildInfoSettings
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin, DocusaurusPlugin, MdocPlugin, ScalaUnidocPlugin)

lazy val dependencySettings = Seq(
  libraryDependencies ++= Seq(
    "com.propensive" %% "magnolia" % magnoliaVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.typelevel" %% "cats-free" % catsVersion,
    "org.apache.avro" % "avro" % avroVersion
  ),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-testkit" % catsVersion,
    "org.slf4j" % "slf4j-nop" % "1.7.26"
  ).map(_ % Test),
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.10.3" cross CrossVersion.binary)
)

lazy val mdocSettings = Seq(
  mdoc := run.in(Compile).evaluated,
  scalacOptions --= Seq("-Xfatal-warnings", "-Ywarn-unused"),
  crossScalaVersions := Seq(scalaVersion.value),
  unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(core),
  target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
  cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
  docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value,
  // format: off
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-doc-source-url", s"https://github.com/ovotech/vulcan/tree/v${(latestVersion in ThisBuild).value}€{FILE_PATH}.scala",
    "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath
  )
  // format: on
)

lazy val buildInfoSettings = Seq(
  buildInfoPackage := "vulcan.build",
  buildInfoObject := "info",
  buildInfoKeys := Seq[BuildInfoKey](
    scalaVersion,
    scalacOptions,
    sourceDirectory,
    latestVersion in ThisBuild,
    moduleName in core,
    organization in root,
    crossScalaVersions in root,
    BuildInfoKey("avroVersion" -> avroVersion),
    BuildInfoKey("catsVersion" -> catsVersion),
    BuildInfoKey("magnoliaVersion" -> magnoliaVersion),
    BuildInfoKey("shapelessVersion" -> shapelessVersion)
  )
)

lazy val metadataSettings = Seq(
  organization := "com.ovoenergy",
  organizationName := "OVO Energy Limited",
  organizationHomepage := Some(url("https://ovoenergy.com"))
)

lazy val publishSettings =
  metadataSettings ++ Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := sonatypePublishTo.value,
    pomIncludeRepository := (_ => false),
    homepage := Some(url("https://ovotech.github.io/vulcan")),
    licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    startYear := Some(2019),
    headerLicense := Some(
      de.heikoseeberger.sbtheader.License.ALv2(
        s"${startYear.value.get}",
        organizationName.value
      )
    ),
    excludeFilter.in(headerSources) := HiddenFileFilter,
    developers := List(
      Developer(
        id = "vlovgr",
        name = "Viktor Lövgren",
        email = "github@vlovgr.se",
        url = url("https://vlovgr.se")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ovotech/vulcan"),
        "scm:git@github.com:ovotech/vulcan.git"
      )
    ),
    releaseCrossBuild := true,
    releaseUseGlobalVersion := true,
    releaseTagName := s"v${(version in ThisBuild).value}",
    releaseTagComment := s"Release version ${(version in ThisBuild).value}",
    releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      setLatestVersion,
      releaseStepTask(updateSiteVariables in ThisBuild),
      releaseStepTask(addDateToReleaseNotes in ThisBuild),
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      releaseStepCommand("sonatypeRelease"),
      setNextVersion,
      commitNextVersion,
      pushChanges,
      releaseStepCommand("docs/docusaurusPublishGhpages")
    )
  )

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := {
    if (publishArtifact.value)
      binaryCompatibleVersions.value
        .map(version => organization.value %% moduleName.value % version)
    else
      Set.empty
  },
  mimaBinaryIssueFilters ++= {
    import com.typesafe.tools.mima.core._
    // format: off
    Seq()
    // format: on
  }
)

lazy val noPublishSettings =
  publishSettings ++ Seq(
    skip in publish := true,
    publishArtifact := false
  )

lazy val scalaSettings = Seq(
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused",
    "-Ypartial-unification",
    "-language:experimental.macros"
  ),
  scalacOptions in (Compile, console) --= Seq("-Xlint", "-Ywarn-unused"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val testSettings = Seq(
  logBuffered in Test := false,
  parallelExecution in Test := false,
  testOptions in Test += Tests.Argument("-oDF")
)

def minorVersion(version: String): String = {
  val (major, minor) =
    CrossVersion.partialVersion(version).get
  s"$major.$minor"
}

val validateDocs = taskKey[Unit]("Validate documentation")
validateDocs in ThisBuild := (run in (docs, Compile)).toTask(" ").value

val releaseNotesFile = taskKey[File]("Release notes for current version")
releaseNotesFile in ThisBuild := {
  val currentVersion = (version in ThisBuild).value
  file("notes") / s"$currentVersion.markdown"
}

val updateSiteVariables = taskKey[Unit]("Update site variables")
updateSiteVariables in ThisBuild := {
  val file = (baseDirectory in root).value / "website" / "siteConfig.js"
  val lines = IO.read(file).trim.split('\n').toVector

  val variables =
    Map[String, String](
      "organization" -> (organization in root).value,
      "moduleName" -> (moduleName in core).value,
      "latestVersion" -> (latestVersion in ThisBuild).value,
      "scalaMinorVersion" -> minorVersion((scalaVersion in root).value),
      "scalaPublishVersions" -> {
        val minorVersions = (crossScalaVersions in root).value.map(minorVersion)
        if (minorVersions.size <= 2) minorVersions.mkString(" and ")
        else minorVersions.init.mkString(", ") ++ " and " ++ minorVersions.last
      }
    )

  val newLine =
    variables.toList
      .map { case (k, v) => s"$k: '$v'" }
      .mkString("const buildInfo = { ", ", ", " };")

  val lineIndex = lines.indexWhere(_.trim.startsWith("const buildInfo"))
  val newLines = lines.updated(lineIndex, newLine)
  val newFileContents = newLines.mkString("", "\n", "\n")
  IO.write(file, newFileContents)

  sbtrelease.Vcs.detect((baseDirectory in root).value).foreach { vcs =>
    vcs.add(file.getAbsolutePath).!
    vcs
      .commit(
        s"Update site variables for v${(version in ThisBuild).value}",
        sign = true,
        signOff = false
      )
      .!
  }
}

val ensureReleaseNotesExists = taskKey[Unit]("Ensure release notes exists")
ensureReleaseNotesExists in ThisBuild := {
  val currentVersion = (version in ThisBuild).value
  val notes = releaseNotesFile.value
  if (!notes.isFile) {
    throw new IllegalStateException(
      s"no release notes found for version [$currentVersion] at [$notes]."
    )
  }
}

val addDateToReleaseNotes = taskKey[Unit]("Add current date to release notes")
addDateToReleaseNotes in ThisBuild := {
  ensureReleaseNotesExists.value

  val dateString = {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val now = java.time.ZonedDateTime.now()
    now.format(formatter)
  }

  val file = releaseNotesFile.value
  val newContents = IO.read(file).trim + s"\n\nReleased on $dateString.\n"
  IO.write(file, newContents)

  sbtrelease.Vcs.detect((baseDirectory in root).value).foreach { vcs =>
    vcs.add(file.getAbsolutePath).!
    vcs
      .commit(
        s"Add release date for v${(version in ThisBuild).value}",
        sign = true,
        signOff = false
      )
      .!
  }
}

def addCommandsAlias(name: String, values: List[String]) =
  addCommandAlias(name, values.mkString(";", ";", ""))

addCommandsAlias(
  "validate",
  List(
    "clean",
    "coverage",
    "test",
    "coverageReport",
    "mimaReportBinaryIssues",
    "scalafmtCheck",
    "scalafmtSbtCheck",
    "headerCheck",
    "doc"
  )
)