The language of ZIO
===================

Concept:
* act on the execution of the effect
* compose effect sequentially
* compose effect in parallel
* act on the success channel
* act on the error channel
* act on environment channel
* resource management

<style type="text/css">
.tg  {border-collapse:collapse;border-color:#ccc;border-spacing:0;}
.tg td{background-color:#fff;border-color:#ccc;border-style:solid;border-width:1px;color:#333;
  font-family:Arial, sans-serif;font-size:14px;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{background-color:#f0f0f0;border-color:#ccc;border-style:solid;border-width:1px;color:#333;
  font-family:Arial, sans-serif;font-size:14px;font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-0pky{border-color:inherit;text-align:left;vertical-align:top}
.tg .tg-fymr{border-color:inherit;font-weight:bold;text-align:left;vertical-align:top}
.tg .tg-f8tv{border-color:inherit;font-style:italic;text-align:left;vertical-align:top}
.tg .tg-3ib7{border-color:inherit;font-family:"Courier New", Courier, monospace !important;;text-align:center;vertical-align:top}
.tg .tg-r5us{font-family:"Courier New", Courier, monospace !important;;text-align:center;vertical-align:top}
.tg .tg-0lax{text-align:left;vertical-align:top}
</style>
<table class="tg">
<thead>
  <tr>
    <th class="tg-0pky">Type</th>
    <th class="tg-fymr" colspan="2">ZIO</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td class="tg-0pky" colspan="3"><span style="font-weight:bold">Sequential execution</span></td>
  </tr>
  <tr>
    <td class="tg-f8tv">combinator</td>
    <td class="tg-f8tv">synonym</td>
    <td class="tg-f8tv">definition</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zip e2</td>
    <td class="tg-3ib7">e1 &lt;*&gt; e2</td>
    <td class="tg-0pky">Sequentially execute e1 and e2 and combine the result into a tuple</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zipRight e2</td>
    <td class="tg-3ib7">e1 *&gt; e2 </td>
    <td class="tg-0pky">idem but keep e2</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zipLeft e2</td>
    <td class="tg-3ib7">e1 &lt;* e2</td>
    <td class="tg-0pky">idem but keep e1</td>
  </tr>
  <tr>
    <td class="tg-0pky"><span style="font-weight:bold">Parallel execution</span></td>
    <td class="tg-0pky"></td>
    <td class="tg-0pky"></td>
  </tr>
  <tr>
    <td class="tg-f8tv">combinator</td>
    <td class="tg-f8tv">synonym</td>
    <td class="tg-f8tv">definition</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zipPar e2</td>
    <td class="tg-3ib7">e1 &lt;&amp;&gt; e2</td>
    <td class="tg-0pky">Parallely execute e1 and e2 and combine the result into a tuple</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zipParRight e2</td>
    <td class="tg-3ib7">e1 &amp;&gt; e2 </td>
    <td class="tg-0pky">idem but keep e2</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 zipParLeft e2</td>
    <td class="tg-3ib7">e1 &lt;&amp; e2</td>
    <td class="tg-0pky">idem but keep e1</td>
  </tr>
  <tr>
    <td class="tg-0pky" colspan="3">Racing execution</td>
  </tr>
  <tr>
    <td class="tg-fymr">combinator</td>
    <td class="tg-fymr">synonym</td>
    <td class="tg-fymr">definition</td>
  </tr>
  <tr>
    <td class="tg-3ib7">e1 raceEither e2</td>
    <td class="tg-3ib7">e1 &lt;|&gt; e2</td>
    <td class="tg-0pky">return the first to succeed as Either[A,B]</td>
  </tr>
  <tr>
    <td class="tg-r5us">e1 race e2</td>
    <td class="tg-r5us"></td>
    <td class="tg-0lax">return the first to succeed if they are of the same type</td>
  </tr>
  <tr>
    <td class="tg-r5us">e1 raceFist e2</td>
    <td class="tg-r5us"></td>
    <td class="tg-0lax">return the first to finish (whatever result E/A)</td>
  </tr>
  <tr>
    <td class="tg-r5us">raceAll Iterable[e]</td>
    <td class="tg-r5us"></td>
    <td class="tg-0lax">return the first to succeed</td>
  </tr>
  <tr>
    <td class="tg-0pky">Compose effects in parallel</td>
    <td class="tg-0pky"></td>
    <td class="tg-0pky"></td>
  </tr>
  <tr>
    <td class="tg-fymr">combinator</td>
    <td class="tg-fymr">synonym</td>
    <td class="tg-fymr">definition</td>
  </tr>
  <tr>
    <td class="tg-0pky" colspan="3">Compose effects in parallel</td>
  </tr>
  <tr>
    <td class="tg-fymr">combinator</td>
    <td class="tg-fymr">synonym</td>
    <td class="tg-fymr">definition</td>
  </tr>
  <tr>
    <td class="tg-0pky" colspan="3">Compose effects in parallel</td>
  </tr>
  <tr>
    <td class="tg-fymr">combinator</td>
    <td class="tg-fymr">synonym</td>
    <td class="tg-fymr">definition</td>
  </tr>
  <tr>
    <td class="tg-0pky" colspan="3">Compose effects in parallel</td>
  </tr>
  <tr>
    <td class="tg-0pky">combinator</td>
    <td class="tg-0pky">synonym</td>
    <td class="tg-0pky">definition</td>
  </tr>
</tbody>
</table>
