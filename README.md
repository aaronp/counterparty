# Data Driven Development

This project is an example of data-driven development -- an approach which treats application flow as data,
which allows it to become the "single point of truth" (tm) for the business logic of your application.


# Benefits of This Approach 🎁
Representing the control flow of your application as data has many benefits:

### 🤩 A clear single point-of truth for your software flow
The [agile manifesto](https://agilemanifesto.org/) evangelises working software over documentation.
We still need documentation, however, so this example generates documentation (a sequence diagram) from the program logic

### 🚀 Composable Logic

Pass around your programs like data, which lets you compose/build bigger programs from small building-blocks

### ⭐ Reduced effort and faster feedback loops
Allow your stakeholders (product people, testers, etc) to play around with changes faster, and in a more
meaningful way, as the same code / logic which drives the backend can be visualised/tested in the browser.

# About This project

See [here](./docs/about.md) to read more about this project and how it works.

Check [here](./docs/building.md) for more about building/running this project.

# Further Examples 

For more examples of leveraging data-driven development:

👉 See [this example of RAFT in your browser](https://github.com/aaronp/riffd) which makes spinning up new nodes
in a cluster as easy as opening a new browser tab! 💪💪💪

👉 Or [this example](https://aaronp.github.io/freemonad/) where the logic for an ETL data enrichment pipeline can be [tested in your browser](https://aaronp.github.io/freemonad/)

It also demonstrates how you can run "What If?" scenarios on your application (e.g. generate reports of
how your software would behave when given certain inputs), allowing quality-assurance engineers to quickly
review hundreds of permutations of scenarios.

Also see [Daniel Spiewak's excellent talk](https://www.youtube.com/watch?v=aKUQUIHRGec) (and [here too](https://www.youtube.com/watch?v=cxMo1RMsD0M)) where he derives the Free Monad.

For more about IO and effect types, see [John DeGoes's wonderful 'FP to the Min'](https://www.youtube.com/watch?v=mrHphQT4RpU) 