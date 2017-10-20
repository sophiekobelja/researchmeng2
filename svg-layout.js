/**
 * Created by Vito on 4/1/17.
 */
// var nodes = [
//     {index: 0, name: "m1()", type: "CM"},
//     {index: 1, name: "m2()", type: "CM"},
//     {index: 2, name: "f1", type: "AF"}
// ];
//
// var links = [
//     {source: 0, target: 2},
//     {source: 1, target: 2}
// ];

var svg = d3.select("svg"),
    width = svg.attr("width"),
    height = svg.attr("height"),
    g = svg.append("g").attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

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

// var circle1 = g.append("g").selectAll("circle")
//   .data(nodes)
//   .enter().append("circle")
//   .attr("r", 12)
//   .attr("fill", function(d) {
//     if (d.type === 'CM' || d.type === 'AM' || d.type === 'DM') return '#1a26ff';
//     else return '#ea8f5a';
//   });

var circle = g.append("g").selectAll('circle')
    .data(nodes)
  .enter().append('g')
    .attr('node-index', function(d) {
      return d.index;
    })
    .on('click', function() {
      var nodeIndex = d3.select(this).attr('node-index');
      var currentNode = nodes[nodeIndex];
      d3.select('#script-title').text(currentNode.type + currentNode.index + ": " + currentNode.name);
      var script = nodes[nodeIndex].script;
      var classname = nodes[nodeIndex].classname;
      var tbody = d3.select('#script-tbody');
      d3.selectAll('circle').attr('r', 30);
      d3.select(this).select('circle').attr('r', 100);
      tbody.selectAll('tr').remove();
      if (script !== undefined) {
        var tr = tbody.selectAll('tr').data(script).enter().append('tr');
        tr.append('td').text(function(d) { return d.type; });
        tr.append('td').text(function(d) { return d.label; });
        tr.append('td').text(function(d) { return d.value; });
          tr.append('td').text(classname);
          tr.append('td').text("references " + currentNode.reference);
          //TODO: add d.referencesToRange: as dictionary of references where key is currentNode.name

      }
      else {
          //var tr = tbody.append('tr');
          tbody.append('td').text(classname);
          tbody.append('td').text("references " + currentNode.reference);
      }
      FillFileContents(currentNode);
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

function FillFileContents(currentNode) {
    var bugname = d3.select("#bug-name").attr("value");
    var fillfilecontents = d3.select('#filecontents');
    fillfilecontents.append('div').text(bugname);
    $.get("derby/" + bugname + "/from/" + currentNode.classname + ".java", function (response) {
        //fillfilecontents.append('div').text(response);
        //var refArray = currentNode.reference.split(",");
        var linenumberstart = response.substring(0, currentNode.reference[0]).split("\n").length;
        var linenumberend = linenumberstart + response.substring(currentNode.reference[0], currentNode.reference[0] + currentNode.reference[1]).split("\n").length;
    });
}