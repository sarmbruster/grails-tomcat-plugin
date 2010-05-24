scriptScope=grails.util.BuildScope.WAR

includeTargets << grailsScript("_GrailsWar" )
	
ant.taskdef(name:"deploy",classname:"org.apache.catalina.ant.DeployTask")
ant.taskdef(name:"list",classname:"org.apache.catalina.ant.ListTask")
ant.taskdef(name:"undeploy",classname:"org.apache.catalina.ant.UndeployTask")



target(main: '''\
Script used to interact with remote Tomcat. The following subcommands are available:

grails tomcat deploy - Deploy to a tomcat server
grails tomcat undeploy - Undeploy from a tomcat server
''') {
    depends(parseArguments, compile,createConfig)

	def cmd = argsMap.params ? argsMap.params[0] : 'deploy'
	argsMap.params.clear()
	def user = config.tomcat.deploy.username ?: 'manager'
	def pass = config.tomcat.deploy.password ?: 'secret'	
	def url = config.tomcat.deploy.url ?: 'http://localhost:8080/manager'
    if (! (url instanceof List)) {
        url = [ url ]
    }

	switch(cmd) {
		case 'deploy':
			war()
            url.each {
                println "Deploying application $serverContextPath to Tomcat $it"
                event("PreDeploy", [it])
                deploy(war:warName,
                       url:it,
                       path:serverContextPath,
                       username:user,
                       password:pass)
                event("PostDeploy", [it])
            }
		break
		case 'list':
            url.each {
                println "Listing applications of Tomcat $it"
                list(
                       url:it,
                       username:user,
                       password:pass)
            }
		break
		case 'undeploy':
			configureServerContextPath()
			println '''\
NOTE: If you experience a classloading error during undeployment you need to take the following steps:					

* Upgrade to Tomcat 6.0.20 or above
* Pass this system argument to Tomcat: -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false

See http://tomcat.apache.org/tomcat-6.0-doc/config/systemprops.html for more information
'''
            url.each {
                println "Undeploying application $serverContextPath from Tomcat $it"
                event("PreUndeploy", [it])
                undeploy(
                       url:it,
                       path:serverContextPath,
                       username:user,
                       password:pass)
                event("PostUndeploy", [it])
            }
        break
        case "redeploy":
            war()
            configureServerContextPath()
            url.each { 
                println "Undeploying application $serverContextPath from Tomcat $it"
                event("PreUndeploy", [it])
                undeploy(
                       url:it,
                       path:serverContextPath,
                       username:user,
                       password:pass)
                event("PostUndeploy", [it])
                event("PreDeploy", [it])
                println "Deploying application $serverContextPath to Tomcat $it"
                deploy(war:warName,
                       url:it,
                       path:serverContextPath,
                       username:user,
                       password:pass)
                event("PostDeploy", [it])
            }
	}
}

def _list = {
    url.each {
        println "Listing applications of Tomcat $it"
        list(
               url:it,
               username:user,
               password:pass)
    }

}

setDefaultTarget("main")
