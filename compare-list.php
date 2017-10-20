<?php
/**
 * Created by PhpStorm.
 * User: Vito
 * Date: 4/3/17
 * Time: 1:06 PM
 */
require_once 'inc/lib.php';

$pdo = getConnection();
$sql1 = "SELECT COUNT(*) FROM edit_script_mapping";
$stmt1 = $pdo->query($sql1);
$result1 = $stmt1->fetch(PDO::FETCH_ASSOC);
$totalRow = $result1['COUNT(*)'];
$stmt1 = null;


if (!isset($_GET['page']) || $_GET['page'] === '')
  $page = 1;
else
  $page = intval($_GET['page']);

$pageCapacity = 20;
$offset = $pageCapacity * ($page - 1);

$sql2 = "SELECT bn1, gn1, bn2, gn2, mapping_num FROM edit_script_mapping LIMIT 20 OFFSET $offset";
$stmt2 = $pdo->query($sql2);
//$result2 = $stmt2->fetch(PDO::FETCH_ASSOC);



?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>List of Mapping</title>
  <link rel="stylesheet" href="css/bootstrap.css">
  <script src="js/jquery.js"></script>
  <script src="js/tether.js"></script>
  <script src="js/bootstrap.js"></script>
</head>
<body>
<div class="container">
  <h2>Edit Script Mapping List</h2>
  <table class="table">
    <thead class="thead-default">
    <tr>
      <th>Bug Name 1</th>
      <th>Graph Num 1</th>
      <th>Bug Name 2</th>
      <th>Graph Num 2</th>
      <th>Mapping Num</th>
      <th>Open</th>
    </tr>
    </thead>
    <tbody>
    <?php
    while (($result2 = $stmt2->fetch(PDO::FETCH_ASSOC)) !== false) {
      $bn1 = $result2['bn1'];
      $gn1 = $result2['gn1'];
      $bn2 = $result2['bn2'];
      $gn2 = $result2['gn2'];
      $mn = $result2['mapping_num'];
      echo <<<AAA
<tr>
<td>$bn1</td>
<td>$gn1</td>
<td>$bn2</td>
<td>$gn2</td>
<td>$mn</td>
<td><a href="compare.php?bn1=$bn1&gn1=$gn1&bn2=$bn2&gn2=$gn2&mapping_num=$mn" target="_blank">Open</a></td>
</tr>
AAA;

    }
    ?>
    </tbody>
  </table>
  <nav>
    <ul class="pagination">
      <li class="page-item"><a class="page-link" href="compare-list.php?page=<?=$page-1?>">Previous</a></li>
      <li class="page-item"><a class="page-link active"><?=$page?></a></li>
      <li class="page-item"><a class="page-link" href="compare-list.php?page=<?=$page+1?>">Next</a></li>
    </ul>
  </nav>
</div>
</body>