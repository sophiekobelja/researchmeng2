<?php
/**
 * Created by PhpStorm.
 * User: Vito
 * Date: 4/1/17
 * Time: 6:40 PM
 */

// Get type string for reference node
function getNodeType($i) {
  $nodeTypeMap = [
    0 => 'UNKNOWN',
    1 => 'CM',
    2 => 'AM',
    3 => 'DM',
    4 => 'AF',
    5 => 'DF'
  ];
  return $nodeTypeMap[$i];
}

// Get type string for reference edge
function getEdgeType($i) {
  $edgeTypeMap = [
    1 => 'FIELD_ACCESS',
    2 => 'METHOD_INVOKE',
    3 => 'METHOD_OVERRIDE'
  ];
  return $edgeTypeMap[$i];
}

// Get PDO object of Sqlite database connection
function getConnection() {
  return new PDO('sqlite:/Users/buttercups1234/Documents/Test.db');
}