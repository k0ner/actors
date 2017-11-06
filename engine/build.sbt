name := s"${Build.namePrefix}-engine"

mainClass in(Compile, run) := Some("octostore.InventoryApp")
