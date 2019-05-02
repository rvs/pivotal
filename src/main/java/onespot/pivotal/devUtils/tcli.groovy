package onespot.pivotal.devUtils

import onespot.pivotal.api.PivotalTracker
import onespot.pivotal.api.resources.Label
import onespot.pivotal.api.resources.Story
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter

import static onespot.pivotal.api.dao.IterationsDAO.IterationsScope

// parse command line options
def options = new Object() {
    @Parameter
    public List<String> args = new ArrayList<>()

    @Parameter(names = "--token", description = "Pivotal tracker token")
    public String token = System.getenv('TRACKER_TOKEN')

    @Parameter(names = "--project", description = "Pivotal project")
    public Integer project = (System.getenv('TRACKER_PROJECT') ?: '0').toInteger()

    @Parameter(names = "--iteration", description = "Pick iteration to operate on: current or backlog", variableArity = true)
    public List<String> iterations = [ "current_backlog" ]

    @Parameter(names = "--sprint", description = "Provide sprint tag")
    public String sprint = "sprint-${new Date().format('yyyyMMdd').toString()}"

    @Parameter(names = "--help", help = true)
    public boolean help = false
}
def jcommander = JCommander.newBuilder().addObject(options).build()
    jcommander.parse(args)
    jcommander.setProgramName('tcli')

// pre-flight checks
if (!options.token || !options.project || options.help) {
    jcommander.usage()
    println "\nDon't forget to specify Pivotal tracker token via TRACKER_TOKEN env variable or --token option"
    return 1
}

// now the real work begins
def pivotalTracker = new PivotalTracker(options.token)
def pivotalProject = pivotalTracker.projects().id(options.project)

List<Story> stories = []
options.iterations.each {
   pivotalProject.iterations().scope( it as IterationsScope).getAll().each { i ->
       stories.addAll(i.getStories())
   }
}

// now lets see what we were asked to do
if (options.args.findIndexOf { it =~ /stamp/ } != -1) {
  Label sprint = new Label()
  sprint.name = options.sprint

  def nakedStories = stories.findResults {
      (it.getCurrentState() != Story.StoryState.accepted &&
       (it.getLabels().findIndexOf { it.name =~ /^sprint-/ } == -1)) ? it : null
  }

  nakedStories.each {
    pivotalProject.stories().id(it.getId()).labels().add(sprint)
  }
}

stories.each {
    println "${it.getId()}: ${it.getCreatedAt()}"
}

println "Total ${stories.size()}"

return 0
