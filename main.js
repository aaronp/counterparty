import './style.css'
import 'scalajs:main.js'
import mermaid from 'mermaid';

  function renderMermaid(targetElm) {
    console.log("rendering " + targetElm);
    
    mermaid.initialize({ startOnLoad: false });
    var future = mermaid.run({
       nodes: [targetElm],
       suppressErrors: false,
    });

    future.then(() => {
      console.log('Rendered!');
    });
  }

  window.renderMermaid = renderMermaid;

  export default { renderMermaid };