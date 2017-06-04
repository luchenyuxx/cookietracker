package models

import models.Graph.{Edge, Node}

/**
  * Created by chenyu on 05/06/2017.
  */
case class Graph(nodes: Set[Node], edges: Set[Edge])

object Graph {
  type Id = String
  case class Node(id: Id, label: String, x: Long, y: Long, size: Long, color: String)
  case class Edge(id: Id, source: Id, target: Id)
}

