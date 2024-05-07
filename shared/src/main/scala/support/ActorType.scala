package support

enum ActorType:
  case Person, Database, Queue, Email, Service, Job, FileSystem

  def icon = this match
    case Person     => "👤"
    case Database   => "🗄️"
    case Queue      => "📤"
    case Email      => "📧"
    case Service    => "🖥️"
    case Job        => "🤖"
    case FileSystem => "📁"
