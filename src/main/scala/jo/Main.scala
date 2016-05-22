package jo

import scala.collection.mutable

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

case class DNR(id: String) {
  override def toString = id
}

case class Reference(from: DNR, to: DNR) {
  def edge = s"""  "$from" -> "$to";"""
}

object Main extends App {

  def gimmeTxt: Seq[File] = {
    val root = new File("src/main/resources/html")
    root.listFiles().toSeq.filter(_.getPath.endsWith(".txt"))
  }

  def readFile(file: File) = {
    new String(Files.readAllBytes(Paths.get(file.toURI)))
  }

  def transitiveClosure(refs: Set[Reference]): Set[Reference] = {
    val grouped = refs.groupBy(_.from).mapValues(_.map(_.to))
    val G = mutable.Map.empty[DNR, Set[DNR]].withDefaultValue(Set.empty[DNR])
    def iter(dnr: DNR): Unit = {
      G.update(dnr, grouped(dnr))
      grouped(dnr).foreach { x =>
        iter(x)
        G.update(dnr, G(dnr) ++ G(x) + x)
      }
    }
    G.keys.foreach(iter)
    val map = G.result()
    (for {
      key <- map.keys
      value <- map(key)
    } yield Reference(key, value)).toSet
  }

  def edge(from: DNR, to: DNR) = s"""  "$from" -> "$to";"""

  def dotFile(references: Set[Reference]) = {
    val sb = new StringBuilder
    sb.append("digraph jo {\n")
    references.foreach { x =>
      sb.append(x.edge)
    }
    sb.append("}")
    sb.toString()
  }

  val dnrRegepx = "[Dd][Nn][Rr] ([0-9]+-\\d\\d\\d\\d)".r
  val fileRegexp = ".*/(.*).txt".r

  def getReferences(textFiles: Seq[File]) = {
    val references = Set.newBuilder[Reference]
    textFiles.foreach { file =>
      val text = readFile(file)
      val dnrNumber = fileRegexp
        .findFirstMatchIn(file.getAbsolutePath)
        .get
        .group(1)
        .replaceAll("[ ,].*", "")
      dnrRegepx.findAllMatchIn(text).foreach { dnrMatch =>
        val reference = dnrMatch.group(1)
        if (dnrNumber != reference) {
          references += Reference(DNR(dnrNumber), DNR(reference))
        }
      }
    }
    references.result()
  }

  def runGraphviz(): Unit = {
    import sys.process._
    Seq("dot", "-Tsvg", "target/out.dot", "-o", "target/out.svg").!
  }

  def writeDotFile(): Unit = {
    val refs = getReferences(gimmeTxt)
    Files.write(Paths.get("target", "out.dot"), dotFile(refs).getBytes)
  }

  writeDotFile()
  runGraphviz()
  println("Completed JO!")
}
