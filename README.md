# Hello Tyrian

## What is it?!

This project is related to my Pull Request: 

https://github.com/PurpleKingdomGames/tyrian/pull/195

With help of `url-dsl` library I baked simple working front-end routing solution which works pretty well with `Tyrian`. There are several URLs: 

1. `/home`
2. `/counter` 
3. `/todos`
4. `/id/:userId`, where `userId` is meant to be `Int`
5. `/user` with query parameter `age` (also meant to be `Int`).


All the other URLs are redirected to custom `notFound` page. Besides of routing I also added two simple subcomponents to show how to modularize application. 

## Setup instructions

To run the program in a browser you will need to have yarn (or npm) installed.

Before your first run and for your tests to work, **you must** install the node dependencies with:

```sh
yarn install
```

This example uses Parcel.js as our bundler and dev server, there are lots of other options you might prefer like Webpack, scalajs-bunder, or even just vanilla JavaScript.

We recommend you have two terminal tabs open in the directory containing this README file.

In the first, we'll run sbt.

```sh
sbt
```

From now on, we can recompile the app with `fastLinkJS` or `fullLinkJS` _**but please note** that the `tyrianapp.js` file in the root is expecting the output from `fastLinkJS`_.

Run `fastLinkJS` now to get an initial build in place.

Then start your dev server, with:

```sh
yarn start
```

Now navigate to [http://localhost:1234/](http://localhost:1234/) to see your site running.

If you leave parcel's dev server running, all you have to do is another `fastLinkJS` or `fullLinkJS` and your app running in the browser should hot-reload the new code.
