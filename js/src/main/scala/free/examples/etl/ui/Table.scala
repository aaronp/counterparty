package free.examples.etl.ui

import org.scalajs.dom.html.{Div, Input, TableRow}
import org.scalajs.dom.{MouseEvent, Node, document, window}

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import scala.scalajs.js.annotation.*
//import scala.scalajs.js.annotation.*

//import scalatags.Text.all.*
import org.scalajs.dom.{MouseEvent, window}
import scalatags.JsDom.all.*

import java.time.{ZoneId, ZonedDateTime}

case class Row(values: Seq[String])

object Row:
  def apply(first: String, values: String*): Row = new Row(first +: values.toSeq)

case class Table(tableId: String, initialRows: IndexedSeq[Row] = Vector()) {
  private var rows     = initialRows
  private val colCount = rows.headOption.fold(0)(_.values.size)

  private var body = TableBody(this, rows.tail)

  def newRow = Row((0 until colCount).map(_ => ""))

  val headerRow = initialRows.headOption match {
    case Some(Row(values)) =>
      val cells = values.map(v => td(v))
      tr(td(`style` := "width:10px") +: cells)
    case None => tr()
  }

  def replaceRows(newRows: IndexedSeq[Row]): Unit = {
    rows = initialRows.headOption ++: newRows
    refreshTable()
  }

  def newTable = table(`id` := tableId)(headerRow.render +: body.bodyRows).render

  private val tblContainer = div(newTable).render

  def refreshTable() = {
    tblContainer.innerHTML = ""
    body = TableBody(this, rows.tail)
    tblContainer.appendChild(newTable)
  }

  def addRow(row: Row): Node = {
    rows = initialRows.headOption ++: rowsForCells() :+ row
    refreshTable()
  }

  /** @param row
    *   the new row
    * @param byIndex
    *   the index of the field to replace if this row is the same as another
    * @return
    *   the refreshed table
    */
  def upsertRow(row: Row, byIndex: Int): Node = {
    val previous  = rowsForCells()
    val thisValue = row.values(byIndex)
    val indexOpt = previous.zipWithIndex.collectFirst {
      case (r, i) if thisValue == r.values(byIndex) => i
    }
    val newRows = indexOpt match {
      case Some(i) => previous.patch(i, Seq(row), 1)
      case None    => previous :+ row
    }
    rows = initialRows.headOption ++: newRows
    refreshTable()
  }

  def rowsForCells() = body.rowsForCells()

  val render = {
    val add = button("Add").render
    add.onclick = e => {
      addRow(newRow)
      e.preventDefault()
    }
    div(tblContainer, div(add))
  }
}

case class TableBody(parent: Table, initialRows: IndexedSeq[Row]) {

  private def cellFor(cellValue: String) = input(`type` := "text", `value` := cellValue)(cellValue)

  private var rowsByIndex: IndexedSeq[(Int, Seq[Input])] = initialRows.zipWithIndex.map {
    (row, idx) =>
      val cells = row.values.map(v => cellFor(v).render)
      (idx, cells)
  }

  def rowsForCells(): IndexedSeq[Row] = rowsByIndex.map { (_, cells) =>
    Row(cells.map(_.value))
  }

  val bodyRows = rowsByIndex.map { (rowIndex, cells) =>
    val deleteMe = button("❌").render
    deleteMe.onclick = e => {
      rowsByIndex = rowsByIndex.patch(rowIndex, Nil, 1)
      parent.replaceRows(rowsForCells())
      e.preventDefault()
    }
    tr(
      td(`style` := "width:10px;align:center")(deleteMe).render +: cells.map(c => td(c).render)
    ).render
  }
}

object Table:
  def apply(id: String, row: Row, theRest: Row*): Table = new Table(id, row +: theRest.toIndexedSeq)
