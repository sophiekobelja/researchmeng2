/**
 * Created by Vito on 3/31/17.
 */
var canvas = document.querySelector("canvas");
var context = canvas.getContext("2d");
var width = canvas.width;
var height = canvas.height;

var nodes = [
    {index: 0, name: "m1()", type: "CM"},
    {index: 1, name: "m2()", type: "CM"},
    {index: 2, name: "f1", type: "AF"}
];

var links = [
    {source: 0, target: 2},
    {source: 1, target: 2}
];

var simulation = d3.forceSimulation(nodes)
    .force("charge", d3.forceManyBody())
    .force("link", d3.forceLink(links))
    .on("tick", ticked);

// d3.select(canvas).call();

function ticked() {
    context.clearRect(0, 0, width, height);
    context.save();
    context.translate(width / 2, height / 2);

    context.beginPath();
    links.forEach(drawLink);
    context.strokeStyle = "#aaa";
    context.stroke();

    context.beginPath();
    nodes.forEach(drawNode);
    context.fill();
    context.strokeStyle = "#fff";
    context.stroke();

    context.restore();
}

function drawLink(d) {
    context.moveTo(d.source.x, d.source.y);
    context.lineTo(d.target.x, d.target.y);
}

function drawNode(d) {
    context.moveTo(d.x + 10, d.y);
    context.arc(d.x, d.y, 10, 0, 2 * Math.PI);
}