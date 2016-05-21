package jo

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths


case class DNR(value: String)
case class Reference(from: DNR, to: DNR)

object Main extends App {
  def gimmeTxt: Seq[File] = {
    val root = new File("src/main/resources/html")

    root.listFiles().toSeq.filter(_.getPath.endsWith(".txt"))
  }

  def readFile(file: File) = {
    new String(Files.readAllBytes(Paths.get(file.toURI)))
  }

  val dnrRegepx = "[Dd][Nn][Rr] ([0-9]+-\\d\\d\\d\\d)".r
  val fileRegexp = ".*/(.*).txt".r

  val sb = new StringBuilder
  gimmeTxt.foreach { file =>
    val text = readFile(file)
    val dnrNumber = fileRegexp
      .findFirstMatchIn(file.getAbsolutePath)
      .get
      .group(1)
      .replaceAll("[ ,].*", "")
    dnrRegepx.findAllMatchIn(text).foreach { dnrMatch =>
      val reference = dnrMatch.group(1)
      if (dnrNumber != reference) {
        sb.append(s"$dnrNumber, $reference\n")
      }
    }
  }
  Files.write(Paths.get("target", "out.csv"), sb.toString().getBytes)
}
