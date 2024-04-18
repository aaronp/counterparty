# Roadmap


⭐⭐ Interpreter 1: Interaction Diagram:
 * Lay out the components in an arc
 * Show a message going from one to another with a tool tip
 * Add play / pause button

 * Add json input to change the svg
 --

⭐ Interpreter 2: Basic IDE json input => output



 * Simplify the mermaid generator code so it can be shared between the two
 * add soucecode dep for exceptions, represent the excepions as data
 * package the server and UI code
 
## Demo
 0. Introduce this as a dive - show Lanzarote pics. This time I'm the guide:
   * We're going to go down slowly - if anybody has any pain presurising, we'll wait and equalise. 
   * I've given us plenty of time - aim for talk to be 50% of the total
   * What are the artefacts we're diving for? This pilot was around just the benefits, 
     so we know if we want to even bother calculating the costs.

     Those benefits are the artefacts we're diving for.

   * Beware / Warning! What this shows is simple, but unfamiliar. Don't run off ahead - stay wth the group!

 1. dive to .5 meters: set the scene - 
    Ok - before we start out dive, let's check-in and take a benchmark.
    we all saw all the onboarding docs and videos. ask everyone to put in the chat their confidence level, 0-10

 1. Remind people that technology exists to help people, not the other way around. It's a tool.
    We don't start with a hammer before we decide what we need to build.

    What we need to build is this app - and it's complex. So the challenge is to:
      * ensure we're on the same page (puzzles in heads)
      * ensure the biggest positive impact can be made by the most people. There are two ways of building software ... so simple there are no obvious bugs. We need to leverage our collective experience, not be burdened by it.
      * immediate feedback. Inventing on Principle ... our ability to experiment and try new things quickly will be the success or failure of this.

 2. This 2 week experiment was about a new tool -- a tecnology choice which helps us address those problems and opportunities. We wanted to be clear on the benefits, before we decided to even spend any effort in calculating the costs.
 
    explain the dive-plan: single point of truth, separate concerns.
    
    show stack of colored bars showing the extraction of logic (what) from how. Killer use-cases: 
    * single point of truth: 
      - eliminates a category of errors, such as drift between docs and working software
      - eliminates confustion -- show people with puzzle pieces in heads
    * promotes collaboration:
      - product, designers, arhitects and devs are aligned and contributing to the same artefacts. Writing code is akin to taking meeting notes
    * immediate feedback. See how changes impact the whole system
    * Effort Multiplier: 
      - great ideas can and do come from anywhere. we want the greatest posible impact made from the most people who require the least context
      - we're letting tecnology work for us.
      - Even if you could hire individuals in roles to do all the different pieces, it would still be slower, and have more points of failure/confusion. 
    * Bonus: By the way, the code is cleaner and simpler, because you've separated different concerns. There are objectively fewer parts to reason about


 3. Let's go! Let's go down to 1 meter:
     * Remind that we're separating out the logic from the how, so we have two things: business logic (code) and interpreters (code)
     * Show the logic: our single point of truth.
       These are our "meeting notes" - we just need secretaries to take them 
       Check in:
       - can people read this?
       - Do people feel that, if after a workshop, they could understand it?
       - Have we found any artefacts yet? 
         
 4. Let's go down to 2 meters: 
    - We've had a quick look at the logic, now let's look at an interpreter: The sequence diagrams.
      This is the key artefact of what we agreed this pilot would try and show -- can we have code as our single point of truth
      and generate the diagrams?
      Check in: have we found any artefacts?

 5. Ok ... anybody want to go any deeper? Ok, let's go down to three meters
    We've seen what the code does... does anybody want to run it?
    - technology exists to help people, not the other way around. If you're doing web stuff => svelte and htmx. Data? Python and Jupyter. AI? Tensorflow and ___. 
    - this approach is agnostic, but if we choose a technology which can target both JS and Java, we can now run the same code in the browser as what will be running on our server.
    - show the basic json input => json output test button for one app
    - check in: How's the pressure? Any Artefacts?
6. So ... Anybody want to go down to 5 meters? We'll need another tank of air to have a peek at how does it work?
   - let's have a quick primer on what this is. Who here is NOT familiar with generics in programming?
     List[Int], List[String], List[Json]  <--  Any type can now have the feature "array"
     Set[Int], Set[Boolean] ... <-- Any type can now be "unique"
     Option[String] (or Maybe[String]) <-- optional / safe nullable values
     Promise[Int] (or Future[Boolean]) <-- concurrency

     ...
     all of these have the same _shape_:  F[A], giving the 'A' some new concept (multiplicity, concurrency, etc)

     ... let's look at some less familiar ones:
     Try[Int], <-- either a result OR a failure
     ID[A] => A <-- "Identity" ... just ignore the wrapper! An ID[A] has the same shape as everything else, but it's just always A
     IO[Int] <-- perhaps less familiar, but this is a lazy future. It also let's you safely cancel, retry, etc. Super cool.
     
     -- check in: how's the pressure? this is just vanilla programming stuff -- I'm just showing you a pattern
     All we're doing here is introducing a new 'F'

     Program[Int], Program[String], Program[Unit] <-- this our logic. It's a kind of tree structure which represents what our program does.

     That was our separation, and we had to then mix in an 'interpreter' to "run" that program and turn that into something useful.

     Our sequence diagram did this:
     Program[A] => List[String]

     To turn it into a runnable JS program, we did this:
     Program[A] => IO[Json]

     Check in:
      - This artefact looks like a plan ... e.g. a whole separate dive,
      - but at a basic level, does that make sense? Who have we lost?
      - ... because I'

7. Phew! Anybody want to go to 10 meters? Who wants too see the whole system?
   - any thoughts on how we might do that?

   * It's another interpreter. 
     I wanted a Program[A] => ID[SVG]

    We've generated the diagrams, and also working code. But ... this is just Program[A] ... it's just code. 
    These thing _compose_.
    Just like you can _compose_ two lists: List(1,2,3) ++ List(4,5,6) => List(1,2,3,4,5,6)

    Well, let's compose the whole app, shall we?

    Check in:
     - how's the pressure? have we found any artefacts?
     - we're taking exactly the same code, and multiplying the benefits. We can now reason about the whole system from any one change.
       We can see the impact we have.
8. Anybody want to go to 15 meters? 
   This is a back-end project after all. Let's show the puzzle-piece and combine contract-first with logic as data.

    - it compiles to both JS and JVM. We've seen the JS, now let's see the JVM
    - Lets have our data structures generated from the REST contracts
    - To do this, we'll have to revisit our tech choices: 
       we picked a language / technology carefully -- one which can target JS and JVM.
       If we want to keep doing that, our OpenAPI stubs would also have to be able to target both.
       Luckily, somebody wrote one (show my merged PR -- thanks William / Li!)
    - show the contract in the logic repo, and runnng REST service in docker
    - take the test data from JS and curl it into or running docker container.
    - check in: artefacts? pressure?

 9. Anybody want to go deeper?
   - The next step is to deploy the JVM ... this bit is blocked.
   - That's for another time - show youtube video of K8S + Argo

 10. Ok - we're really running out of air here ... best come up.
    let's review our dive:
    - this pilot was about looking for benefits, and if they were worth the cost. Let's look at what we have, and see if we agree we want to put that on the scale:

    - show the artefacts and a scale.

    - this helps inform us where do we go next. These are simple things - we've taken a lot of simple things and combined them in a new, unfamiliar way.
      We've been diving ... but a single molecule of water isn't wet. You only get wetnesss when you put a bunch together ... It's an "emergent property"

      We've separated some concerns, shown they're simple / not scary, but then put them together to come up with our artefacts. 

    - Suggestion:
      * workshops / training -- you've seen the outputs of one person in 2 weeks.
        Shall we see if that can scale?
      * we don't have any _real_ JVM interpreters. We have to actually write data to _real_ databases and queues. Do you want to see if we can do that?
    
   


## Next:
 * Either Distributed Tracing or step-wise interpreter 




 -- more notes:

  * Programs are just things which return vaues:
    Program[A]
    and you can make bigger programs made up by other litle programs:
    Program[C] = Program[A] and then run Program[B]

    We can use 'types' to help us construct these programs safely, as they prevent a certain 
    category of errors from occurring, such as tring to add a number to some text.

    We often refer to these types as shapes, as they are little puzzle pieces. 

    Types can get unweildly, but equally, programming without them is like trying to build a puzzle
    with smooth edge pieces.

 * "Functional Programming" has a bad name - but it's just shapes and types. Just like I can't say "I know two
people who got divorced, so marriage is bad", it's the same thing with FP. It can be used badly.


 * 