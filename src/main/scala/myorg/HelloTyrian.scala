package myorg

import cats.effect.IO
import cats.effect.implicits.*
import cats.effect.kernel.*
import cats.syntax.all.*
import myorg.Page.getPage
import myorg.components.TodoItem
import myorg.components.TodoItemList
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.PopStateEvent
import org.scalajs.dom.document
import tyrian.Html.{param => _, _}
import tyrian.SVG.*
import tyrian.*
import tyrian.cmds.Logger
import urldsl.errors.SimplePathMatchingError
import urldsl.language.PathSegment
import urldsl.language.PathSegmentWithQueryParams
import urldsl.language.simpleErrorImpl.*
import urldsl.vocabulary.Param
import urldsl.vocabulary.Segment
import urldsl.vocabulary.UrlMatching

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("TyrianApp")
object HelloTyrian extends TyrianApp[Msg, Model]:

  def init(flags: Map[String, String]): (Model, Cmd[IO, Msg]) =
    (
      AppState(
        dom.window.location.href.getPage(),
        counter = 0,
        modalOpened = false,
        TodoItemList.Model.init()
      ),
      Cmd.SideEffect(
        dom.window.history.pushState({}, "Home", dom.window.location.href)
      )
    )

  def update(model: Model): Msg => (Model, Cmd[IO, Msg]) =
    case Msg.Increment  => (model.copy(counter = model.counter + 1), Cmd.None)
    case Msg.Decrement  => (model.copy(counter = model.counter - 1), Cmd.None)
    case Msg.Reset      => (model.copy(counter = 0), Cmd.None)
    case Msg.Void       => (model, Cmd.None)
    case Msg.OpenModal  => (model.copy(modalOpened = true), Cmd.None)
    case Msg.CloseModal => (model.copy(modalOpened = false), Cmd.None)
    case Msg.Modify(msg) =>
      (
        model.copy(todoList = TodoItemList.update(msg, model.todoList)),
        Cmd.None
      )
    case Msg.GoToCounterPage =>
      val newModel = model.copy(page = Page.Counter)

      (
        newModel,
        Cmd.SideEffect(
          dom.window.history
            .pushState({}, "Counter", newModel.page.asString())
        )
      )
    case Msg.GoToHomePage =>
      val newModel = model.copy(page = Page.Home)

      (
        newModel,
        Cmd.SideEffect(
          dom.window.history
            .pushState({}, "Home", newModel.page.asString())
        )
      )
    case Msg.URLChanged =>
      val newPage  = dom.window.location.href.getPage()
      val newModel = model.copy(page = newPage)

      (
        newModel,
        Cmd.None
      )

  def viewHome(model: Model): Html[Msg] =
    div(
      p("Hello from home!"),
      button(onClick(Msg.GoToCounterPage))("Go counter")
    )

  def viewCounter(model: Model): Html[Msg] =
    div(`class` := "flex flex-col justify-center items-center")(
      p(`class` := "mb-2 justify-self-center")("Counter Application"),
      div(`class` := "flex flex-row")(
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mr-2",
          onClick(Msg.Increment)
        )(
          "+"
        ),
        p(model.counter.toString()),
        button(
          `class` := "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded ml-2",
          onClick(Msg.Decrement)
        )("-")
      ),
      if (model.counter != 0)
        button(
          `class` := "bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded mt-2",
          onClick(Msg.Reset)
        )("Reset")
      else
        button(
          `class` := "bg-red-300 text-gray-700 font-bold py-2 px-4 rounded mt-2 cursor-not-allowed"
        )("Reset"),
      button(onClick(Msg.GoToHomePage))("Go to homepage")
    )

  def viewModal(): Html[Msg] =
    div(
      button(
        styles(
          "data-modal-target" -> "popup-modal",
          "data-modal-toggle" -> "popup-modal"
        ),
        `class` := "block text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800",
        `type` := "button"
      )("Open Modal"),
      div(
        id       := "popup-modal",
        tabIndex := -1,
        `class` := "fixed top-0 left-0 right-0 hidden z-50 p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full"
      )(
        div(`class` := "relative w-full max-w-md max-h-full")(
          div(
            `class` := "relative bg-white rounded-lg shadow dark:bg-gray-700"
          )(
            button(
              `type` := "button",
              `class` := "absolute top-3 right-2.5 text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-800 dark:hover:text-white",
              styles("data-modal-hide" -> "popup-modal")
            )(
              "Close Modal"
            )
          )
        )
      )
    )

  def viewNotFound(model: Model): Html[Msg] =
    div(
      p("Not found the requested page"),
      button(
        styles(
          "data-modal-target" -> "defaultModal",
          "data-modal-toggle" -> "defaultModal"
        ),
        `class` := "block text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800",
        `type` := "button",
        onClick(Msg.OpenModal)
      )("Open Modal"),
      div(
        id       := "defaultModal",
        tabIndex := -1,
        styles("aria-hidden" -> "true"),
        `class` := s"fixed top-0 left-0 right-0 z-50 ${
            if (!model.modalOpened) "hidden" else ""
          } p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full"
      )(
        div(`class` := "relative w-full max-h-full")(
          div(
            `class` := "relative bg-white rounded-lg shadow dark:bg-gray-700"
          )(
            div(
              `class` := "flex items-start justify-between p-4 border-b rounded-t dark:border-gray-600"
            )(
              h3(
                `class` := "text-xl font-semibold text-gray-900 dark:text-white"
              )("Terms of Service"),
              button(
                `type` := "button",
                `class` := "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-gray-900 rounded-lg text-sm p-1.5 ml-auto inline-flex items-center dark:hover:bg-gray-600 dark:hover:text-white",
                styles("data-modal-hide" -> "defaultModal"),
                onClick(Msg.CloseModal)
              )(
                svg(
                  `class` := "w-5 h-5",
                  fill    := "currentColor",
                  viewBox := "0 0 20 20",
                  xmlns   := "http://www.w3.org/2000"
                )(
                  SVG.path(
                    styles("fill-rule" -> "evenodd", "clip-rule" -> "evenodd"),
                    d := "M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  )
                )
              )
            ),
            div(`class` := "p-6 space-y-6")(
              p(
                `class` := "text-base leading-relaxed text-gray-500 dark:text-gray-400"
              )(
                " With less than a month to go before the European Union..."
              )
            ),
            div(
              `class` := "lex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600"
            )(
              button(
                styles("data-modal-hide" -> "defaultModal"),
                `type` := "button",
                `class` := "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800",
                onClick(Msg.CloseModal)
              )(
                "I Accept"
              ),
              button(
                styles("data-modal-hide" -> "defaultModal"),
                `type` := "button",
                `class` := "text-white bg-red-700 hover:bg-red-800 focus:ring-4 focus:outline-none focus:ring-red-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-red-600 dark:hover:bg-red-700 dark:focus:ring-red-800",
                onClick(Msg.CloseModal)
              )(
                "I Decline"
              )
            )
          )
        )
      ),
      button(onClick(Msg.GoToHomePage))("Go to homepage"),
      div(`class` := "w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700")(
        div(
          `class` := "bg-blue-600 h-2.5 rounded-full",
          styles(CSS.width("100%"))
        )()
      ),
      div(styles("role" -> "status"), `class` := "max-w-sm animate-pulse")(
        div(
          `class` := "h-2.5 bg-gray-200 rounded-full dark:bg-gray-700 w-48 mb-4"
        )(),
        div(
          `class` := "h-2 bg-gray-200 rounded-full dark:bg-gray-700 max-w-[360px] mb-2.5"
        )(),
        div(
          `class` := "h-2 bg-gray-200 rounded-full dark:bg-gray-700 mb-2.5"
        )(),
        div(
          `class` := "h-2 bg-gray-200 rounded-full dark:bg-gray-700 max-w-[330px] mb-2.5"
        )(),
        div(
          `class` := "h-2 bg-gray-200 rounded-full dark:bg-gray-700 max-w-[300px] mb-2.5"
        )()
      ),
      div(`class` := "flex items-center")(
        svg(
          styles(
            "aria-hidden" -> "true",
            "fill"        -> "currentColor",
            "viewBox"     -> "0 0 20 20",
            "xmlns"       -> "http://www.w3.org/2000/svg"
          ),
          `class` := "w-5 h-5 text-yellow-400"
        )(
          title("First Star"),
          path(
            d := "M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          )
        ),
        svg(
          styles(
            "aria-hidden" -> "true",
            "fill"        -> "currentColor",
            "viewBox"     -> "0 0 20 20",
            "xmlns"       -> "http://www.w3.org/2000/svg"
          ),
          `class` := "w-5 h-5 text-yellow-400"
        )(
          title("First Star"),
          path(
            d := "M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          )
        ),
        svg(
          styles(
            "aria-hidden" -> "true",
            "fill"        -> "currentColor",
            "viewBox"     -> "0 0 20 20",
            "xmlns"       -> "http://www.w3.org/2000/svg"
          ),
          `class` := "w-5 h-5 text-yellow-400"
        )(
          title("First Star"),
          path(
            d := "M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          )
        ),
        svg(
          styles(
            "aria-hidden" -> "true",
            "fill"        -> "currentColor",
            "viewBox"     -> "0 0 20 20",
            "xmlns"       -> "http://www.w3.org/2000/svg"
          ),
          `class` := "w-5 h-5 text-yellow-400"
        )(
          title("First Star"),
          path(
            d := "M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          )
        ),
        svg(
          styles(
            "aria-hidden" -> "true",
            "fill"        -> "currentColor",
            "viewBox"     -> "0 0 20 20",
            "xmlns"       -> "http://www.w3.org/2000/svg"
          ),
          `class` := "w-5 h-5 text-gray-300 dark:text-gray-500"
        )(
          title("First Star"),
          path(
            d := "M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          )
        )
      )
    )

  def viewUserPage(userId: Int): Html[Msg] =
    div(
      p(s"Welcome at user page with user id: ${userId}"),
      button(onClick(Msg.GoToHomePage))("Go to home")
    )

  def viewUserMaybeAgePage(ageOption: Option[Int]): Html[Msg] =
    ageOption.fold(div(p("You're so boring...")))(age =>
      div(p(s"Thanks, you are cool and have $age years!"))
    )

  def viewTodosPage(model: Model): Html[Msg] =
    TodoItemList.view(model.todoList).map(msg => Msg.Modify(msg))

  def view(model: Model): Html[Msg] =
    model.page match {
      case Page.Home             => viewHome(model)
      case Page.Counter          => viewCounter(model)
      case Page.NotFound         => viewNotFound(model)
      case Page.Todos            => viewTodosPage(model)
      case Page.UserPage(userId) => viewUserPage(userId)
      case Page.UserAgePage(age) => viewUserMaybeAgePage(age)
    }

  val popstateEvent: Sub[IO, Msg] =
    Sub.fromEvent("popstate", dom.window) { (e: PopStateEvent) =>
      Option(Msg.URLChanged)
    }

  def subscriptions(model: Model): Sub[IO, Msg] =
    popstateEvent

enum Page {
  case Home
  case Counter
  case NotFound
  case Todos
  case UserPage(userId: Int)
  case UserAgePage(ageOption: Option[Int])

}

object Page {
  val homePath        = root / "home"
  val counterPath     = root / "counter"
  val todoPath        = root / "todos"
  val moreComplexPath = root / "id" / segment[Int]
  val pathWithParam   = (root / "user" / endOfSegments) ? param[Int]("age").?

  case class SimpleRoute[X, P](
      pathSegment: PathSegment[X, SimplePathMatchingError],
      combinator: X => P
  )

  case class ComplexRoute[X, Y, P](
      pathSegment: PathSegmentWithQueryParams[X, SimplePathMatchingError, Y, ?],
      combinator: UrlMatching[X, Y] => P
  )

  def routerFromList(
      xs: List[SimpleRoute[?, Page] | ComplexRoute[?, ?, Page]],
      path: String
  ): Page =
    xs match {
      case Nil => Page.NotFound
      case (head: SimpleRoute[?, Page]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => Page.NotFound, head.combinator(_))
      case (head: ComplexRoute[?, ?, Page]) :: Nil =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => Page.NotFound, head.combinator(_))
      case (head: SimpleRoute[?, Page]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => routerFromList(tail, path), head.combinator(_))
      case (head: ComplexRoute[?, ?, Page]) :: tail =>
        head.pathSegment
          .matchRawUrl(path)
          .fold[Page](_ => routerFromList(tail, path), head.combinator(_))
    }

  extension (p: Page)
    def asString(): String = p match {
      case Counter          => "/counter"
      case Home             => "/home"
      case Todos            => "/todos"
      case NotFound         => "/notfound"
      case UserPage(userId) => s"/id/${userId}"
      case UserAgePage(ageOption) =>
        ageOption match {
          case None      => "/user"
          case Some(age) => s"/user?age=${age}"
        }
    }

  extension (path: String)
    def getPage(): Page = routerFromList(
      List(
        SimpleRoute[Unit, Page](homePath, _ => Page.Home),
        SimpleRoute[Unit, Page](counterPath, _ => Page.Counter),
        SimpleRoute[Unit, Page](todoPath, _ => Page.Todos),
        SimpleRoute[Int, Page](
          moreComplexPath,
          (userId: Int) => Page.UserPage(userId)
        ),
        ComplexRoute[Unit, Option[Int], Page](
          pathWithParam,
          { case UrlMatching(_, ageOption) => Page.UserAgePage(ageOption) }
        )
      ),
      path
    )

}

case class AppState(
    page: Page,
    counter: Int,
    modalOpened: Boolean,
    todoList: TodoItemList.Model
)

type Model = AppState

enum Msg:
  case Increment, Decrement, Reset
  case Modify(msg: TodoItemList.Msg)
  case Void
  case GoToCounterPage
  case GoToHomePage
  case OpenModal
  case CloseModal
  case URLChanged
