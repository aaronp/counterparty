Yes, you can install mermaid.min.js using npm (Node Package Manager). Installing via npm allows you to manage your project dependencies more effectively and integrate Mermaid with modern build tools like Webpack, Vite, or Rollup.

Here's how you can install and use Mermaid with npm:

Step 1: Install Mermaid with npm
Ensure you have a Node.js environment set up with npm. Then, you can install Mermaid in your project using npm.

bash
Copy code
# Install Mermaid using npm
npm install mermaid
This command adds Mermaid to your project's node_modules directory and updates the package.json file to include Mermaid as a dependency.

Step 2: Import Mermaid into Your JavaScript/Scala.js Project
Once installed, you can import Mermaid into your JavaScript or Scala.js project. In JavaScript, use import to bring Mermaid into scope.

JavaScript
javascript
Copy code
import mermaid from 'mermaid';

// Initialize Mermaid
mermaid.initialize({ startOnLoad: true });
In this example, Mermaid is imported and initialized to start rendering diagrams when the page loads.

Scala.js
If you're using Scala.js, you need to set up Scala.js to work with npm-based packages. You can import Mermaid from the node_modules directory and interact with it from Scala.js.

scala
Copy code
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// Import Mermaid from node_modules
@js.native
@JSImport("mermaid", JSImport.Default)
object Mermaid extends js.Object {
  def initialize(options: js.Dynamic): Unit = js.native
  def init(selector: String): Unit = js.native
}

@JSExportTopLevel("MermaidScalaJS")
object MermaidScalaJS {
  def main(): Unit = {
    // Initialize Mermaid with default settings
    Mermaid.initialize(js.Dynamic.literal(startOnLoad = true))
    
    // Additional configurations can be set as needed
    // For example, you can initialize specific selectors:
    // Mermaid.init("#specificDiagram")
  }
}
In this example, Scala.js imports Mermaid from npm and initializes it with initialize(). You can also use init() to render specific diagrams.

Step 3: Integrate Mermaid with Build Tools
When working with build tools like Webpack or Vite, ensure the build configuration includes Mermaid. This may involve ensuring the build tool processes JavaScript correctly and includes npm-based packages.

Conclusion
To install mermaid.min.js with npm, add Mermaid as a dependency to your project with npm install mermaid. Then import and initialize Mermaid in your JavaScript or Scala.js code. This allows you to manage dependencies effectively and integrate Mermaid with modern build tools and development workflows.