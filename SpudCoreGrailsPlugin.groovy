import groovy.json.JsonSlurper

import spud.core.SpudCacheWebKeyGenerator

class SpudCoreGrailsPlugin {
    def version = "0.7.1"
    def grailsVersion = "2.3 > *"
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def loadAfter = ['cache']

    def title           = "Spud Core Plugin"
    def author          = "David Estes"
    def authorEmail     = "destes@bcap.com"
    def description     = "Spud Admin is a dependency package that adds a nice looking administrative panel to any project you add it to. It supports easy grails app integration and provides core functionality for spud modules."

    def documentation   = "https://github.com/spud-grails/spud-core"
    def license         = "APACHE"
    def organization    = [name: "Bertram Labs", url: "http://www.bertramlabs.com/"]
    def issueManagement = [system: "GITHUB", url: "https://github.com/spud-grails/spud-core/issues"]
    def scm             = [url: "https://github.com/spud-grails/spud-core"]


    def doWithDynamicMethods = {
        String.metaClass.camelize = {
          delegate.split("-").inject(""){ before, word ->
            before += word[0].toUpperCase() + word[1..-1]
          }
        }

        String.metaClass.underscore = {
            def output = delegate.replaceAll("-","_")
            output.replaceAll(/\B[A-Z]/) { '_' + it }.toLowerCase()
        }

        String.metaClass.humanize = {
            def output = delegate.replaceAll(/[\_\-]+/," ")
        }

        String.metaClass.parameterize = {
            def output = delegate.replaceAll(/[^A-Za-z0-9\-_]+/,"-").toLowerCase()
        }

        String.metaClass.titlecase = {
            def output = delegate.replaceAll( /\b[a-z]/, { it.toUpperCase() })
        }

    }

    def doWithSpring = {
        def beanName = application.config.spud.securityService ? application.config.spud.securityService : 'abstractSpudSecurityService'
        springConfig.addAlias "spudSecurity", beanName

        def multiSiteServiceName = application.config.spud.multiSiteService ?: 'spudDefaultMultiSiteService'
        springConfig.addAlias "spudMultiSiteService", multiSiteServiceName

        application.config.spud.renderers = application.config.spud.renderers ?: [:]
        application.config.spud.layoutEngines = application.config.spud.layoutEngines ?: [:]
        application.config.spud.renderers.gsp = 'defaultSpudRendererService'
        application.config.spud.layoutEngines.system = 'defaultSpudLayoutService'
        application.config.spud.formatters = [
            [name: 'html', description: 'Formatted HTML'],
            [name: 'raw', description: 'RAW HTML']
        ]

		webCacheKeyGenerator(SpudCacheWebKeyGenerator)


        // Load In Cached Layout List
        if(application.warDeployed) {
            application.config.spud.core.layouts = []
            def layoutList = application.parentContext.getResource("WEB-INF/spudLayouts.txt")
            if(layoutList.exists()) {

                def contents = layoutList.inputStream.text
                if(contents) {
                    application.config.spud.core.layouts = contents.split("\n")
                }
            }
        }

    }

    def doWithApplicationContext = { ctx ->
        ctx.adminApplicationService.initialize()
    }

    def getWebXmlFilterOrder() {
        ["SpudMultiSiteFilter": FilterManager.URL_MAPPING_POSITION + 999]
    }

    def doWithWebDescriptor = { xml ->
        def filters = xml.filter[0]
        filters + {
            'filter' {
                'filter-name'('SpudMultiSiteFilter')
                'filter-class'('spud.core.SpudMultiSiteFilter')
            }
        }

        def mappings = xml.'filter-mapping'[0]
        mappings + {
            'filter-mapping' {
                'filter-name'('SpudMultiSiteFilter')
                'url-pattern'("/*")
                dispatcher('REQUEST')
            }
        }
    }
}
