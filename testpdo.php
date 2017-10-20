<?php
/**
 * Created by PhpStorm.
 * User: Vito
 * Date: 4/1/17
 * Time: 6:13 PM
 */
$pdo = new PDO('sqlite:/Users/Vito/Documents/VT/2016fall/SE/patternData.sqlite');
$stmt = $pdo->query("SELECT graph_data FROM edit_script_table WHERE bug_name='421717_DERBY-1459'");
$result = $stmt->fetch(PDO::FETCH_ASSOC);
$stmt = null;
$pdo = null;
