/**
 * Created by Vito on 4/2/17.
 */

var svg = d3.select('#common-graph'),
    width = svg.attr('width'),
    height = svg.attr('height'),
    g = svg.append('g').attr('transform', 'translate(' + width / 2 + ',' + height / 2 + ')');

var simulation = d3.forceSimulation(nodes)
    .force("charge", d3.forceManyBody().strength(-50))
    .force("link", d3.forceLink(links).distance(220))
    .force("collide", d3.forceCollide(50))
    .force("x", d3.forceX())
    .force("y", d3.forceY())
    .on("tick", ticked);

var path = g.append("g").selectAll("path")
  .data(links)
  .enter().append("path")
  .attr("stroke", "#DA45E3")
  .attr("stroke-width", "2px")
  .attr("marker-end", "url(#marker-arrow)");

var circle = g.append("g").selectAll('circle')
  .data(nodes)
  .enter().append('g')
  .attr('node-index', function(d) {
    return d.index;
  })
  .on('click', function() {
    var nodeIndex = d3.select(this).attr('node-index');
    var currentNode = nodes[nodeIndex];
    d3.select('#script-title1').text(currentNode.type + currentNode.index + ": " + currentNode.name);
    d3.select('#script-title2').text(currentNode.type + currentNode.index + ": " + currentNode.newName);
    var script = nodes[nodeIndex].script;
    var newScript = nodes[nodeIndex].newScript;
    var tbody = d3.select('#script-tbody1');
    tbody.selectAll('tr').remove();
    if (script !== undefined) {
      var tr = tbody.selectAll('tr').data(script).enter().append('tr');
      tr.append('td').text(function(d) { return d.type; });
      tr.append('td').text(function(d) { return d.label; });
      tr.append('td').text(function(d) { return d.value; });
    }
    var tbody2 = d3.select('#script-tbody2');
    tbody2.selectAll('tr').remove();
    if (newScript !== undefined) {
      var tr2 = tbody2.selectAll('tr').data(newScript).enter().append('tr');
      tr2.append('td').text(function(d) { return d.type; });
      tr2.append('td').text(function(d) { return d.label; });
      tr2.append('td').text(function(d) { return d.value; });
    }
  });

circle.append('circle')
  .attr('r', 30)
  .attr('fill', function(d) {
    if (d.type === 'CM' || d.type === 'AM' || d.type === 'DM') return '#1a26ff';
    else return '#ea8f5a';
  });

circle.append('text')
  .text(function(d) { return d.type + d.index; })
  .attr('text-anchor', 'middle')
  .attr('fill', '#FAFAFA')
  .attr('font-size', '20px');

function ticked() {
  path.attr("d", linkLine);
  circle.attr("transform", moveNode);
}

function linkLine(d) {
  return "M" + d.source.x + "," + d.source.y + " L" + d.target.x + "," + d.target.y;
}

function moveNode(d) {
  return "translate(" + d.x + "," + d.y + ")";
}
