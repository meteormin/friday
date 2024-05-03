export interface SignupRequest {
    email: string;
    password: string;
    name: string;
}

const USER_ROLE = {
    USER: "USER",
    MANAGER: "MANAGER",
    ADMIN: "ADMIN",
} as const;

type USER_ROLE = typeof USER_ROLE[keyof typeof USER_ROLE];

const SOCIAL_PROVIDER = {
    GOOGLE: "GOOGLE",
    NAVER: "NAVER",
    KAKAO: "KAKAO",
    NONE : "NONE",
} as const;

type SOCIAL_PROVIDER = typeof SOCIAL_PROVIDER[keyof typeof SOCIAL_PROVIDER];

export interface UserResource {
    id: number;
    snsId: string;
    provider: SOCIAL_PROVIDER;
    username: string;
    name: string;
    role: USER_ROLE;
    snsUser: boolean;
    attributes: { [key: string]: any };
    authorities: {
        Authority: string;
    }[];
}

export interface SigninRequest {
    username: string;
    password: string;
}

export interface Tokens {
    tokenType: string;
    accessToken: string;
    expiresIn: number;
    refreshToken: string;
}

export interface SigninResponse {
    id: number;
    username: string;
    name: string;
    tokens: Tokens;
}
