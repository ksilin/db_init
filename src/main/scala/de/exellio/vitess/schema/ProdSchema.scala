package de.exellio.vitess.schema
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

case class Product(sku: String, description: String, price: Int)

case class ProdSchema(dc: DatabaseConfig[JdbcProfile]) {
//  extends StringCollectionMappers
//    with Java8DateTimeMapper {

  import dc.profile.api._
  val db = dc.db

  class ProdTable(tag: Tag, prefix: String) extends Table[Product](tag, s"product") {
    val sku: Rep[String]         = column[String]("sku", O.PrimaryKey, O.Unique)
    val description: Rep[String] = column[String]("description")
    val price: Rep[Int]          = column[Int]("price")

    def * : ProvenShape[Product] = (sku, description, price).mapTo[Product]
  }

  object products extends TableQuery(new ProdTable(_, "article")) {
    val tableName: String = shaped.value.tableName
    val sch               = this.schema
  }
}
