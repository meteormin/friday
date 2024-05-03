package com.meteormin.friday.hosts.application.service;

import com.meteormin.friday.common.error.ForbiddenErrorException;
import com.meteormin.friday.common.hexagon.annotation.Usecase;
import com.meteormin.friday.hosts.application.exception.ExistsSearchException;
import com.meteormin.friday.hosts.application.exception.NotFoundSearchException;
import com.meteormin.friday.hosts.application.port.in.query.RetrieveSearchQuery;
import com.meteormin.friday.hosts.application.port.in.usecase.SearchUsecase;
import com.meteormin.friday.hosts.application.port.out.HostPort;
import com.meteormin.friday.hosts.application.port.out.SearchPort;
import com.meteormin.friday.hosts.domain.searches.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@Usecase
@RequiredArgsConstructor
public class SearchService implements SearchUsecase, RetrieveSearchQuery {
    /**
     * searchPort.
     */
    private final SearchPort searchPort;

    /**
     * hostPort.
     */
    private final HostPort hostPort;

    /**
     * Retrieves the searches based on the given filter.
     *
     * @param filter the search filter
     * @return the page of search results
     */
    @Override
    public Page<Search> retrieveSearches(SearchFilter filter) {
        return searchPort.findSearchAll(filter);
    }

    /**
     * Retrieves a search based on the given search IDs.
     *
     * @param ids the search IDs to retrieve
     * @return the search matching the IDs
     */
    @Override
    public Search retrieveSearch(SearchIds ids) {
        if (inaccessibleToSearch(ids)) {
            throw new ForbiddenErrorException();
        }

        return searchPort.findSearchById(ids.id())
                .orElseThrow(NotFoundSearchException::new);
    }

    /**
     * Creates a search based on the provided search parameters.
     *
     * @param search an object containing the search parameters
     * @return the created search object
     */
    @Override
    public Search createSearch(CreateSearch search) {
        var ids = SearchIds.builder()
                .hostId(search.hostId())
                .userId(search.userId())
                .build();

        if (inaccessibleToSearch(ids)) {
            throw new ForbiddenErrorException();
        }

        var where = WhereSearch.builder()
                .hostId(search.hostId())
                .queryKey(search.queryKey())
                .query(search.query())
                .build();

        if (searchPort.isUniqueSearch(where)) {
            throw new ExistsSearchException();
        }

        return searchPort.createSearch(Search.create(search));
    }

    /**
     * Updates a search based on the provided update information.
     *
     * @param updateSearch the update information for the search
     * @return the updated search
     */
    @Override
    public Search patchSearch(PatchSearch updateSearch) {
        if (inaccessibleToSearch(updateSearch.ids())) {
            throw new ForbiddenErrorException();
        }

        var exists = searchPort.findSearchById(updateSearch.ids().id())
                .orElseThrow(NotFoundSearchException::new);

        exists.patch(updateSearch);
        return searchPort.updateSearch(exists);
    }

    /**
     * Deletes a search by its ID.
     *
     * @param ids the search IDs
     */
    @Override
    public void deleteSearchById(SearchIds ids) {
        if (inaccessibleToSearch(ids)) {
            throw new ForbiddenErrorException();
        }
        var domain = searchPort.findSearchById(ids.id())
                .orElseThrow(NotFoundSearchException::new);

        searchPort.deleteSearchById(domain.getId());
    }

    /**
     * Checks if the given search is inaccessible to search.
     *
     * @param ids the search ids
     * @return true if the search is inaccessible, false otherwise
     */
    private boolean inaccessibleToSearch(SearchIds ids) {
        var host = hostPort.findById(ids.hostId())
                .orElse(null);

        if (host == null) {
            return false;
        }

        if (!host.getUserId().equals(ids.userId())) {
            throw new ForbiddenErrorException();
        }

        return true;
    }
}
