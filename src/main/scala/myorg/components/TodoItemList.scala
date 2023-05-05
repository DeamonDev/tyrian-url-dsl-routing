package myorg.components

import tyrian.Html.*
import tyrian.*

object TodoItemList {
  opaque type Model = List[TodoItem.Model]
  object Model {
    def init(): Model = List(
      TodoItem.Model.init(id = 1, "go 4 shopping"),
      TodoItem.Model.init(id = 2, "meditate"),
      TodoItem.Model.init(id = 3, "play with cats!"),
      TodoItem.Model.init(id = 4, "drink more water")
    )
  }

  def view(model: Model): Html[Msg] =
    div(`class` := "flex flex-col")(
      ul(
        model.map(todoItemModel =>
          li(style("key", todoItemModel.getId().toString()))(
            TodoItem
              .view(todoItemModel)
              .map(msg => Msg.Modify(todoItemModel.getId(), msg))
          )
        )
      ),
      button(`class` := "bg-green-300 hover:bg-green-400 mt-2")("ADD TODO")
    )

  def update(msg: Msg, model: Model): Model =
    msg match {
      case Msg.Modify(id, TodoItem.Msg.RemoveItem) =>
        model.filter(todoItem => todoItem.getId() != id)
      case _ => ???
    }

  enum Msg:
    case Void
    case Modify(id: Int, msg: TodoItem.Msg)
}
