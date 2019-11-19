package it.extracube.items.application;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.util.PortalUtil;

import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.TermQuery;
import com.liferay.portal.search.searcher.SearchRequest;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.SearchResponse;
import com.liferay.portal.search.searcher.Searcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

/**
 * @author luna
 */
@Component(
	property = {
		JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/greetings",
		JaxrsWhiteboardConstants.JAX_RS_NAME + "=Greetings.Rest",
			"auth.verifier.guest.allowed=true",
			"oauth2.scopechecker.type=none",
			"liferay.access.control.disable=true",
		/*	"auth.verifier.auth.verifier.PortalSessionAuthVerifier.urls.includes=/*"*/
	},
	service = Application.class
)
public class ItemsServiceApplication extends Application {

	public Set<Object> getSingletons() {
		return Collections.<Object>singleton(this);
	}

	@GET
	@Produces("text/plain")
	public String working(@Context HttpServletRequest request) {

		try {
			User user = PortalUtil.getUser(request);
			if (user.isDefaultUser()) {
				_log.warn("User not logged.");
				return "User not logged.";
			}

			//http://localhost:9200/liferay-20101/LiferayDocumentType/_search?q=struttura_personale_oggetti&pretty



			TermQuery termquery= _queries.term("_type","LiferayDocumentType");
			SearchRequest searchRequest = searchRequestBuilderFactory.builder().postFilterQuery(termquery).build();

			SearchResponse res=searcher.search(searchRequest);

			List<Document> docs=res.getDocuments71();


			return user.getFullName(); //<-- it works!
		}
		 catch (PortalException e){
			_log.warn("PortalException:"+e.getMessage());
			return "PortalException";

		 }
	}

	@GET
	@Path("/morning")
	@Produces("text/plain")
	public String hello() {
		return "Good morning!";
	}

	@GET
	@Path("/morning/{name}")
	@Produces("text/plain")
	public String morning(
		@PathParam("name") String name,
		@QueryParam("drink") String drink) {

		String greeting = "Good Morning " + name;

		if (drink != null) {
			greeting += ". Would you like some " + drink + "?";
		}

		return greeting;
	}

	@Reference
	private Queries _queries;

	@Reference
	protected Searcher searcher;

	@Reference
	SearchRequestBuilderFactory searchRequestBuilderFactory;

	private Log _log= LogFactoryUtil.getLog(ItemsServiceApplication.class);
}