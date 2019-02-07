package de.exellio.vitess.schema

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

case class Nation(n_nationkey: Int, n_name: String, n_regionkey: Int, n_comment: String)

case class NationSchema(dc: DatabaseConfig[JdbcProfile]) {
//  extends StringCollectionMappers
//    with Java8DateTimeMapper {

  import dc.profile.api._
  val db = dc.db

  class NationTable(tag: Tag, prefix: String) extends Table[Nation](tag, s"nation") {
    val key: Rep[Int]        = column[Int]("n_nationkey", O.PrimaryKey, O.Unique)
    val name: Rep[String]    = column[String]("n_name")
    val region: Rep[Int]     = column[Int]("n_regionkey")
    val comment: Rep[String] = column[String]("n_comment")

    def * : ProvenShape[Nation] = (key, name, region, comment).mapTo[Nation]
  }

  object nations extends TableQuery(new NationTable(_, "article")) {
    val tableName: String = shaped.value.tableName
    val sch               = this.schema
  }
}
