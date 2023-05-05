package myorg.components

import cats.effect.IO
import tyrian.Html.*
import tyrian.*

object TodoItem {
  case class State(id: Int, description: String, isDone: Boolean)

  opaque type Model = State

  object Model {
    def init(id: Int, description: String): Model =
      State(id, description, false)
  }

  extension (m: Model) def getId(): Int = m.id

  def view(model: Model): Html[Msg] =
    div(
      onClick(Msg.RemoveItem),
      `class` := "flex bg-blue-300 hover:bg-cyan-600"
    )(
      p(model.description)
    )

  def update(msg: Msg, model: Model): Model =
    msg match {
      case Msg.RemoveItem => model
      case _              => ???
    }

  enum Msg:
    case RemoveItem
    case ChangeDescription(newDescription: String)

}
