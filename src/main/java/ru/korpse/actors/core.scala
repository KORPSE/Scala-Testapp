import akka.actor.ActorSystem

trait Core {
  protected implicit def system : ActorSystem
}