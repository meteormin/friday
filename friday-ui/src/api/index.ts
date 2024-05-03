import ApiClient, { ErrorResponse } from '@api/base/ApiClient';
import {
    AfterResponseHandler,
    ErrorHandler,
    ErrorResInterface,
    PreRequestHandler,
    Token,
    TokenResponseHandler,
} from '@api/base/types';
import BaseClient from '@api/base/BaseClient';
import { makePath } from '@api/utils/str';
import { AxiosError, AxiosRequestHeaders } from 'axios';

/**
 * ClientType
 */
interface ClientType<T> {
    new (client: ApiClient): T;

    readonly prefix: string;
}

/**
 * Api configuration.
 */
export interface ApiConfig {
    host?: string | null;
    prefix?: string | null;
    headers?: AxiosRequestHeaders;
    token?: Token;
    errorHandler?: ErrorHandler;
    preRequestHandler?: PreRequestHandler;
    afterResponseHandler?: AfterResponseHandler;
}

/**
 * A function that serves as the default error handler.
 *
 * @param {any} error - The error object that is being handled.
 * @return {ErrorResInterface} The error response object.
 */
export const defaultErrorHandler: ErrorHandler = (
    error: any,
): ErrorResInterface => {
    const errorResponse: ErrorResInterface = {
        error: 'UNKNOWN_ERROR',
        code: 0,
        message: 'Unknown Error',
    };

    if (error instanceof AxiosError) {
        if (error.response?.data.error || 500 < 500) {
            errorResponse.error = error.response?.data.error || 'error';
            errorResponse.code =
                error.response?.data.code || error.response?.data.error || 0;
            errorResponse.message = error.response?.data.message || '';
        }
    }

    return {
        ...error.response,
        isSuccess: false,
        error: new ErrorResponse(errorResponse),
    };
};

const defaultConfig: ApiConfig = {
    host: 'localhost',
    prefix: '/api',
    errorHandler: defaultErrorHandler,
};

/**
 * @param {ApiConfig} apiConfig
 * @returns {ApiClient}
 */
export const api = (apiConfig: ApiConfig): ApiClient => {
    if (apiConfig?.prefix) {
        if (!apiConfig?.host) {
            apiConfig.host = defaultConfig.host as string;
        }

        try {
            const url = new URL(apiConfig.host);
            apiConfig.host =
                url.protocol +
                '//' +
                makePath(url.host + url.pathname, apiConfig.prefix);
        } catch (error) {
            const url = new URL(window.location.href);
            apiConfig.host =
                url.protocol +
                '//' +
                makePath(url.host + apiConfig.host, apiConfig.prefix);
        }
    }

    if (apiConfig?.host) {
        try {
            const url = new URL(apiConfig.host);
            apiConfig.host = url.protocol + '//' + url.host + url.pathname;
        } catch (error) {
            const url = new URL(window.location.href);
            apiConfig.host = url.protocol + '//' + url.host + apiConfig.host;
        }
    } else {
        apiConfig.host = defaultConfig.host as string;
    }

    if (!apiConfig?.errorHandler) {
        apiConfig.errorHandler = defaultConfig.errorHandler;
    }

    const client: ApiClient = new ApiClient(
        apiConfig.host,
        apiConfig.errorHandler,
        apiConfig.preRequestHandler,
        apiConfig.afterResponseHandler,
    );

    if (apiConfig) {
        if (apiConfig.headers) {
            client.withHeader(apiConfig.headers);
        }
        if (apiConfig.token) {
            client.withToken(
                apiConfig.token.token as string,
                apiConfig.token.tokenType as string,
            );
        }
    }

    return client;
};

/**
 * Creates a new instance of the specified client type using the given configuration.
 *
 * @param {ClientType<T extends BaseClient>} client - The client type to instantiate.
 * @param {ApiConfig} config - The configuration for the API.
 * @returns {T extends BaseClient} - The new instance of the client type.
 */
export const makeClient = <T extends BaseClient>(
    client: ClientType<T>,
    config: ApiConfig,
): T => {
    return new client(
        api({
            ...config,
            prefix: client.prefix,
        }),
    );
};

export default makeClient;
