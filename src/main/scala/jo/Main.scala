package jo

import scala.collection.mutable

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

case class DNR(id: String) extends AnyVal {
  override def toString = id
}

case class Reference(from: DNR, to: DNR) {
  def edge = s"""  "$from" -> "$to";"""
}

object Main extends App {

  lazy val gimmeTxt: Seq[File] = {
    val root = new File("src/main/resources/txt")
    root.listFiles().toSeq.filter(_.getPath.endsWith(".txt"))
  }

  def readFile(file: File) = {
    new String(Files.readAllBytes(Paths.get(file.toURI)))
  }

  def transitiveClosure(refs: Set[Reference]): Set[Reference] = {
    val children = refs.groupBy(_.from).mapValues(_.map(_.to))
    val C = mutable.Map.empty[DNR, Set[DNR]].withDefaultValue(Set.empty[DNR])
    val visited = mutable.Set.empty[DNR]
    def closure(dnr: DNR): Unit = {
      if (!visited(dnr)) {
        visited.update(dnr, included = true)
        val b = Set.newBuilder[DNR]
        children(dnr).foreach { x =>
          closure(x)
          C(x).foreach(b += _)
        }
        C.update(dnr, b.result())
      }
    }
    refs.foreach(x => closure(x.from))
    val map = C.result()
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

  def extractDnr(file: File): String = {
    fileRegexp
      .findFirstMatchIn(file.getAbsolutePath)
      .get
      .group(1)
      .replaceAll("[ ,].*", "")
  }

  def getMissingDnr: Set[DNR] = {
    val uniqueDnr = gimmeTxt.map(extractDnr).map(DNR.apply).toSet
    val uniqueReferencedDnr = getReferences(gimmeTxt).map(_.to)
    uniqueReferencedDnr.diff(uniqueDnr)
  }

  def getReferences(textFiles: Seq[File]) = {
    val references = Set.newBuilder[Reference]
    textFiles.foreach { file =>
      val text = readFile(file)
      val dnrNumber = extractDnr(file)
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

  def writeDotFile(transitive: Boolean): Unit = {
    val refs = getReferences(gimmeTxt)
    val toRun = if (transitive) transitiveClosure(refs) else refs
    println(toRun == refs)
    Files.write(Paths.get("target", "out.dot"), dotFile(toRun).getBytes)
  }

//  writeDotFile(transitive = true)
//  runGraphviz()
  getMissingDnr.foreach(println)
  println("Completed JO!")
}





