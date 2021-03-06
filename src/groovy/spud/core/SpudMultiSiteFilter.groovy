package spud.core

import javax.servlet.*
import org.springframework.web.context.support.WebApplicationContextUtils
import groovy.util.logging.Log4j

@Log4j
class SpudMultiSiteFilter implements Filter {
    def applicationContext
    def servletContext
	def multiSiteService
    void init(FilterConfig config) throws ServletException {
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
        servletContext = config.servletContext
        multiSiteService = applicationContext['spudMultiSiteService']
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        def site = multiSiteService.siteForUrl(request.requestURL.toString())

		request.setAttribute('spudSiteId', site.siteId)
		request.setAttribute('spudSite', site)
        if (!response.committed) {
            chain.doFilter(request, response)
        }
    }

}
