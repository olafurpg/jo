case class Point(x: Int,y: Int){
  def mult(other:Point)=Point(x*other.x,y*other.y)
}
val p=Point(3,5)
p.x
p.y

val p2=Point(1,2)
p mult p2


case class This(x: String,y: String)

val ja=This("JA","JAJAMENSAN")
val nej=This("NEJ","NEJDÅ")
val kanske=ja.x + " och " + nej.x
val va=ja.y + " men " + nej.x

//ja.copy(p=nej)





object HelloWorld {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
  }
}

// HelloWorld.main(Array())
