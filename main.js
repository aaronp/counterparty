import './style.css'
import 'scalajs:main.js'
import mermaid from 'mermaid';
// import 'golden-layout/dist/css/goldenlayout-base.css';
// import 'golden-layout/dist/css/themes/goldenlayout-dark-theme.css';


/** We call this from MermaidPage, sending it a new element which contains mermaid markdown  */
function renderMermaid(targetElm) {
  mermaid.initialize({ startOnLoad: false });
  mermaid.run({
    nodes: [targetElm],
    suppressErrors: false,
  }).then(() => {
    console.log('Rendered mermaid diagram');
  });
}

window.renderMermaid = renderMermaid;
export default { renderMermaid };
