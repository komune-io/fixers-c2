declare function KtSingleton<T>(): T & (abstract new() => unknown);
export declare interface F2Consumer<T> {
}
export declare interface F2Function<in T, out R> {
}
export declare interface F2Supplier<R> {
}
export declare interface F2SupplierSingle<R> {
}
export declare class Batch {
    constructor(size?: number, concurrency?: number);
    get size(): number;
    get concurrency(): number;
    copy(size?: number, concurrency?: number): Batch;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Batch {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Batch;
    }
}
export declare function invokeF2Function<T, R>(fnc: F2Function<T, R>, msgs: Array<T>): Promise<Array<R>>;
export declare function invokeF2Consumer<T>(fnc: F2Consumer<T>, msgs: Array<T>): Promise<void>;
export declare function invokeF2Supplier<R>(fnc: F2Supplier<R>): Promise<Array<R>>;
export declare function invokeF2SupplierSingle<R>(fnc: F2SupplierSingle<R>): Promise<R>;
export declare interface Command extends Message {
}
export declare interface Event extends Message {
}
export declare interface Message {
}
export declare interface Problem extends Message {
}
export declare interface Query extends Message {
}
export declare interface EnvelopeDTO<T> extends WithEnvelopeId, WithEnvelopeData<T>, Message {
    readonly id: string;
    readonly data: T;
    readonly type: string;
    readonly datacontenttype?: string | null;
    readonly specversion?: string | null;
    readonly source?: string | null;
    readonly time?: string | null;
}
export declare class Envelope<T> implements EnvelopeDTO<T> {
    constructor(id: string, data: T, type: string, datacontenttype?: string | null, source?: string | null, time?: string | null);
    get id(): string;
    get data(): T;
    get type(): string;
    get datacontenttype(): string | null;
    get source(): string | null;
    get time(): string | null;
    get specversion(): string;
    copy(id?: string, data?: T, type?: string, datacontenttype?: string | null, source?: string | null, time?: string | null): Envelope<T>;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Envelope {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <T>() => Envelope<T>;
    }
}
export declare interface WithEnvelopeData<T> {
    readonly data: T;
}
export declare interface WithEnvelopeId {
    readonly id: string;
}
export declare interface F2ErrorDTO extends Problem {
    readonly id?: string | null;
    readonly timestamp: string;
    readonly code: number;
    readonly requestId?: string | null;
    readonly message: string;
    readonly refusal?: F2RefusalDTO | null;
}
export declare interface F2RefusalDTO {
    readonly code: string;
}
export declare class F2Refusal implements F2RefusalDTO {
    constructor(code: string, parameters?: Record<string, string>, citers?: string[]);
    get code(): string;
    copy(code?: string, parameters?: Record<string, string>, citers?: string[]): F2Refusal;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace F2Refusal {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2Refusal;
    }
}
export declare class F2Error implements F2ErrorDTO {
    constructor(message: string, id?: string | null, timestamp?: string, code?: number, requestId?: string | null, refusal?: F2Refusal | null);
    get message(): string;
    get id(): string | null;
    get timestamp(): string;
    get code(): number;
    get requestId(): string | null;
    get refusal(): F2Refusal | null;
    toString(): string;
}
export declare namespace F2Error {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2Error;
    }
}
export declare function isRefusal(error: F2ErrorDTO): boolean;
export declare function refusalCode(error: F2ErrorDTO): string;
export declare abstract class HttpRefusal {
    private constructor();
    static get INVALID_PAYLOAD(): HttpRefusal & {
        get name(): "INVALID_PAYLOAD";
        get ordinal(): 0;
    };
    static get UNAUTHENTICATED(): HttpRefusal & {
        get name(): "UNAUTHENTICATED";
        get ordinal(): 1;
    };
    static get PAYMENT_REQUIRED(): HttpRefusal & {
        get name(): "PAYMENT_REQUIRED";
        get ordinal(): 2;
    };
    static get FORBIDDEN(): HttpRefusal & {
        get name(): "FORBIDDEN";
        get ordinal(): 3;
    };
    static get NOT_FOUND(): HttpRefusal & {
        get name(): "NOT_FOUND";
        get ordinal(): 4;
    };
    static get METHOD_NOT_ALLOWED(): HttpRefusal & {
        get name(): "METHOD_NOT_ALLOWED";
        get ordinal(): 5;
    };
    static get NOT_ACCEPTABLE(): HttpRefusal & {
        get name(): "NOT_ACCEPTABLE";
        get ordinal(): 6;
    };
    static get PROXY_UNAUTHENTICATED(): HttpRefusal & {
        get name(): "PROXY_UNAUTHENTICATED";
        get ordinal(): 7;
    };
    static get REQUEST_TIMEOUT(): HttpRefusal & {
        get name(): "REQUEST_TIMEOUT";
        get ordinal(): 8;
    };
    static get CONFLICT(): HttpRefusal & {
        get name(): "CONFLICT";
        get ordinal(): 9;
    };
    static get GONE(): HttpRefusal & {
        get name(): "GONE";
        get ordinal(): 10;
    };
    static get LENGTH_REQUIRED(): HttpRefusal & {
        get name(): "LENGTH_REQUIRED";
        get ordinal(): 11;
    };
    static get PRECONDITION_FAILED(): HttpRefusal & {
        get name(): "PRECONDITION_FAILED";
        get ordinal(): 12;
    };
    static get PAYLOAD_TOO_LARGE(): HttpRefusal & {
        get name(): "PAYLOAD_TOO_LARGE";
        get ordinal(): 13;
    };
    static get URI_TOO_LONG(): HttpRefusal & {
        get name(): "URI_TOO_LONG";
        get ordinal(): 14;
    };
    static get UNSUPPORTED_MEDIA_TYPE(): HttpRefusal & {
        get name(): "UNSUPPORTED_MEDIA_TYPE";
        get ordinal(): 15;
    };
    static get RANGE_NOT_SATISFIABLE(): HttpRefusal & {
        get name(): "RANGE_NOT_SATISFIABLE";
        get ordinal(): 16;
    };
    static get EXPECTATION_FAILED(): HttpRefusal & {
        get name(): "EXPECTATION_FAILED";
        get ordinal(): 17;
    };
    static get MISDIRECTED_REQUEST(): HttpRefusal & {
        get name(): "MISDIRECTED_REQUEST";
        get ordinal(): 18;
    };
    static get UNPROCESSABLE(): HttpRefusal & {
        get name(): "UNPROCESSABLE";
        get ordinal(): 19;
    };
    static get LOCKED(): HttpRefusal & {
        get name(): "LOCKED";
        get ordinal(): 20;
    };
    static get FAILED_DEPENDENCY(): HttpRefusal & {
        get name(): "FAILED_DEPENDENCY";
        get ordinal(): 21;
    };
    static get TOO_EARLY(): HttpRefusal & {
        get name(): "TOO_EARLY";
        get ordinal(): 22;
    };
    static get UPGRADE_REQUIRED(): HttpRefusal & {
        get name(): "UPGRADE_REQUIRED";
        get ordinal(): 23;
    };
    static get PRECONDITION_REQUIRED(): HttpRefusal & {
        get name(): "PRECONDITION_REQUIRED";
        get ordinal(): 24;
    };
    static get TOO_MANY_REQUESTS(): HttpRefusal & {
        get name(): "TOO_MANY_REQUESTS";
        get ordinal(): 25;
    };
    static get HEADERS_TOO_LARGE(): HttpRefusal & {
        get name(): "HEADERS_TOO_LARGE";
        get ordinal(): 26;
    };
    static get UNAVAILABLE_FOR_LEGAL_REASONS(): HttpRefusal & {
        get name(): "UNAVAILABLE_FOR_LEGAL_REASONS";
        get ordinal(): 27;
    };
    static get UNEXPECTED(): HttpRefusal & {
        get name(): "UNEXPECTED";
        get ordinal(): 28;
    };
    static get UNAVAILABLE(): HttpRefusal & {
        get name(): "UNAVAILABLE";
        get ordinal(): 29;
    };
    static values(): [typeof HttpRefusal.INVALID_PAYLOAD, typeof HttpRefusal.UNAUTHENTICATED, typeof HttpRefusal.PAYMENT_REQUIRED, typeof HttpRefusal.FORBIDDEN, typeof HttpRefusal.NOT_FOUND, typeof HttpRefusal.METHOD_NOT_ALLOWED, typeof HttpRefusal.NOT_ACCEPTABLE, typeof HttpRefusal.PROXY_UNAUTHENTICATED, typeof HttpRefusal.REQUEST_TIMEOUT, typeof HttpRefusal.CONFLICT, typeof HttpRefusal.GONE, typeof HttpRefusal.LENGTH_REQUIRED, typeof HttpRefusal.PRECONDITION_FAILED, typeof HttpRefusal.PAYLOAD_TOO_LARGE, typeof HttpRefusal.URI_TOO_LONG, typeof HttpRefusal.UNSUPPORTED_MEDIA_TYPE, typeof HttpRefusal.RANGE_NOT_SATISFIABLE, typeof HttpRefusal.EXPECTATION_FAILED, typeof HttpRefusal.MISDIRECTED_REQUEST, typeof HttpRefusal.UNPROCESSABLE, typeof HttpRefusal.LOCKED, typeof HttpRefusal.FAILED_DEPENDENCY, typeof HttpRefusal.TOO_EARLY, typeof HttpRefusal.UPGRADE_REQUIRED, typeof HttpRefusal.PRECONDITION_REQUIRED, typeof HttpRefusal.TOO_MANY_REQUESTS, typeof HttpRefusal.HEADERS_TOO_LARGE, typeof HttpRefusal.UNAVAILABLE_FOR_LEGAL_REASONS, typeof HttpRefusal.UNEXPECTED, typeof HttpRefusal.UNAVAILABLE];
    static valueOf(value: string): HttpRefusal;
    get name(): "INVALID_PAYLOAD" | "UNAUTHENTICATED" | "PAYMENT_REQUIRED" | "FORBIDDEN" | "NOT_FOUND" | "METHOD_NOT_ALLOWED" | "NOT_ACCEPTABLE" | "PROXY_UNAUTHENTICATED" | "REQUEST_TIMEOUT" | "CONFLICT" | "GONE" | "LENGTH_REQUIRED" | "PRECONDITION_FAILED" | "PAYLOAD_TOO_LARGE" | "URI_TOO_LONG" | "UNSUPPORTED_MEDIA_TYPE" | "RANGE_NOT_SATISFIABLE" | "EXPECTATION_FAILED" | "MISDIRECTED_REQUEST" | "UNPROCESSABLE" | "LOCKED" | "FAILED_DEPENDENCY" | "TOO_EARLY" | "UPGRADE_REQUIRED" | "PRECONDITION_REQUIRED" | "TOO_MANY_REQUESTS" | "HEADERS_TOO_LARGE" | "UNAVAILABLE_FOR_LEGAL_REASONS" | "UNEXPECTED" | "UNAVAILABLE";
    get ordinal(): 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25 | 26 | 27 | 28 | 29;
    get status(): number;
    get refusal(): string;
}
export declare namespace HttpRefusal {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => HttpRefusal;
    }
    abstract class Companion extends KtSingleton<Companion.$metadata$.constructor>() {
        private constructor();
    }
    namespace Companion {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor {
                get DOMAIN(): string;
                ofStatus(status: number): HttpRefusal;
                private constructor();
            }
        }
    }
}
export declare class F2Exception extends /* RuntimeException */ Error {
    constructor(error: F2ErrorDTO, cause?: Error | null);
    get error(): F2ErrorDTO;
}
export declare namespace F2Exception {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2Exception;
    }
}
export declare interface Match<T> {
    readonly isNegative: boolean;
    map<R>(transform: (value: T) => R): Match<R>;
    not(): Match<T>;
    and(match: Match<T>): Match<T>;
    or(match: Match<T>): Match<T>;
}
export declare interface SortDTO {
    readonly property: string;
    readonly ascending: boolean;
    readonly nullsFirst?: boolean | null;
}
export declare interface PageDTO<out OBJECT> {
    readonly total: number;
    readonly items: OBJECT[];
}
export declare class Page<out OBJECT> implements PageDTO<OBJECT> {
    constructor(total: number, items: OBJECT[]);
    get total(): number;
    get items(): OBJECT[];
    copy(total?: number, items?: OBJECT[]): Page<OBJECT>;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Page {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <OBJECT>() => Page<OBJECT>;
    }
}
export declare interface PageQueryDTO extends Query {
    readonly pagination?: OffsetPaginationDTO | null;
}
export declare interface PageQueryResultDTO<out OBJECT> extends Event, PageDTO<OBJECT> {
    readonly total: number;
    readonly items: OBJECT[];
    readonly pagination?: OffsetPaginationDTO | null;
}
export declare class PageQuery implements PageQueryDTO {
    constructor(pagination?: OffsetPaginationDTO | null);
    get pagination(): OffsetPaginationDTO | null;
    copy(pagination?: OffsetPaginationDTO | null): PageQuery;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace PageQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => PageQuery;
    }
}
export declare class PageQueryResult<out OBJECT> implements PageQueryResultDTO<OBJECT> {
    constructor(pagination: OffsetPagination | null, total: number, items: OBJECT[]);
    get pagination(): OffsetPagination | null;
    get total(): number;
    get items(): OBJECT[];
    copy(pagination?: OffsetPagination | null, total?: number, items?: OBJECT[]): PageQueryResult<OBJECT>;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace PageQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <OBJECT>() => PageQueryResult<OBJECT>;
    }
}
export declare interface Pagination {
}
export declare interface OffsetPaginationDTO extends Pagination {
    readonly offset: number;
    readonly limit: number;
}
export declare interface PagePaginationDTO extends Pagination {
    readonly page?: number | null;
    readonly size?: number | null;
}
export declare class OffsetPagination implements OffsetPaginationDTO {
    constructor(offset: number, limit: number);
    get offset(): number;
    get limit(): number;
    copy(offset?: number, limit?: number): OffsetPagination;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace OffsetPagination {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => OffsetPagination;
    }
}
export declare class PagePagination implements PagePaginationDTO {
    constructor(page?: number | null, size?: number | null);
    get page(): number | null;
    get size(): number | null;
    copy(page?: number | null, size?: number | null): PagePagination;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace PagePagination {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => PagePagination;
    }
}
export declare function refusalParameters(refusal: F2RefusalDTO): ReadonlyMap<string, string>;
export declare function refusalCiters(refusal: F2RefusalDTO): ReadonlyArray<string>;
export declare interface BlockDTO {
    readonly blockId: number;
    readonly previousHash: Int8Array;
    readonly dataHash: Int8Array;
    readonly transactions: TransactionDTO[];
}
export declare class Block implements BlockDTO {
    constructor(blockId: number, previousHash: Int8Array, dataHash: Int8Array, transactions: Transaction[]);
    get blockId(): number;
    get previousHash(): Int8Array;
    get dataHash(): Int8Array;
    get transactions(): Transaction[];
}
export declare namespace Block {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Block;
    }
}
export declare interface ChaincodeUriDTO {
    readonly uri: string;
}
export declare class ChaincodeUri implements ChaincodeUriDTO {
    constructor(uri: string);
    get uri(): string;
    get channelId(): string;
    get chaincodeId(): string;
    copy(uri?: string): ChaincodeUri;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace ChaincodeUri {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => ChaincodeUri;
    }
    abstract class Companion extends KtSingleton<Companion.$metadata$.constructor>() {
        private constructor();
    }
    namespace Companion {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor {
                get PARTS(): number;
                get PREFIX(): string;
                private constructor();
            }
        }
    }
}
export declare interface IdentitiesInfoDTO {
    readonly id: string;
    readonly mspid: string;
}
export declare class IdentitiesInfo implements IdentitiesInfoDTO {
    constructor(id: string, mspid: string);
    get id(): string;
    get mspid(): string;
}
export declare namespace IdentitiesInfo {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => IdentitiesInfo;
    }
}
export declare interface TransactionDTO {
    readonly transactionId: string;
    readonly blockId: number;
    readonly timestamp: number;
    readonly isValid: boolean;
    readonly channelId: string;
    readonly creator: IdentitiesInfoDTO;
    readonly nonce: Int8Array;
}
export declare class Transaction implements TransactionDTO {
    constructor(transactionId: string, blockId: number, timestamp: number, isValid: boolean, channelId: string, creator: IdentitiesInfo, nonce: Int8Array);
    get transactionId(): string;
    get blockId(): number;
    get timestamp(): number;
    get isValid(): boolean;
    get channelId(): string;
    get creator(): IdentitiesInfo;
    get nonce(): Int8Array;
    copy(transactionId?: string, blockId?: number, timestamp?: number, isValid?: boolean, channelId?: string, creator?: IdentitiesInfo, nonce?: Int8Array): Transaction;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Transaction {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Transaction;
    }
}
export declare interface SsmChaincodeQueries {
    ssmGetAdminFunction(): F2Function<SsmGetAdminQuery, SsmGetAdminResult>;
    ssmGetQueryFunction(): F2Function<SsmGetQuery, SsmGetResult>;
    ssmGetSessionLogsQueryFunction(): F2Function<SsmGetSessionLogsQuery, SsmGetSessionLogsQueryResult>;
    ssmGetSessionQueryFunction(): F2Function<SsmGetSessionQuery, SsmGetSessionResult>;
    ssmGetTransactionQueryFunction(): F2Function<SsmGetTransactionQuery, SsmGetTransactionQueryResult>;
    ssmGetUserFunction(): F2Function<SsmGetUserQuery, SsmGetUserResult>;
    ssmListAdminQueryFunction(): F2Function<SsmListAdminQuery, SsmListAdminResult>;
    ssmListSessionQueryFunction(): F2Function<SsmListSessionQuery, SsmListSessionResult>;
    ssmListSsmQueryFunction(): F2Function<SsmListSsmQuery, SsmListSsmResult>;
    ssmListUserQueryFunction(): F2Function<SsmListUserQuery, SsmListUserResult>;
}
export declare interface SsmQueryDTO extends Query {
    readonly chaincodeUri: ChaincodeUri;
}
export declare interface SsmItemResultDTO<T> extends Event {
    readonly item?: T | null;
}
export declare interface SsmItemsResultDTO<T> extends Event {
    readonly items: Array<T>;
}
export declare interface BatchPropertiesDTO {
    readonly timeout: number;
    readonly size: number;
    readonly concurrency: number;
}
export declare class SsmBatchProperties implements BatchPropertiesDTO {
    constructor(timeout?: number, size?: number, concurrency?: number);
    get timeout(): number;
    get size(): number;
    get concurrency(): number;
}
export declare namespace SsmBatchProperties {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmBatchProperties;
    }
}
export declare interface SsmChaincodePropertiesDTO {
    readonly url: string;
}
export declare class ChaincodeSsmConfig implements SsmChaincodePropertiesDTO {
    constructor(url: string);
    get url(): string;
}
export declare namespace ChaincodeSsmConfig {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => ChaincodeSsmConfig;
    }
}
export declare interface AgentDTO {
    readonly name: string;
    readonly pub: Int8Array;
}
export declare class Agent implements AgentDTO {
    constructor(name: string, pub: Int8Array);
    get name(): string;
    get pub(): Int8Array;
    equals(other?: unknown): boolean;
    hashCode(): number;
    copy(name?: string, pub?: Int8Array): Agent;
    toString(): string;
}
export declare namespace Agent {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Agent;
    }
    abstract class Companion extends KtSingleton<Companion.$metadata$.constructor>() {
        private constructor();
    }
    namespace Companion {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor {
                private constructor();
            }
        }
    }
}
export declare interface ChaincodeDTO {
    readonly id: string;
    readonly channelId: string;
}
export declare class Chaincode implements ChaincodeDTO {
    constructor(id: string, channelId: string);
    get id(): string;
    get channelId(): string;
    copy(id?: string, channelId?: string): Chaincode;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Chaincode {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Chaincode;
    }
}
export declare interface SsmDTO {
    readonly name: string;
    readonly transitions: SsmTransitionDTO[];
}
export declare class Ssm implements SsmDTO {
    constructor(name: string, transitions: SsmTransition[]);
    get name(): string;
    get transitions(): SsmTransition[];
    copy(name?: string, transitions?: SsmTransition[]): Ssm;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Ssm {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Ssm;
    }
}
export declare interface SsmContextDTO extends WithPrivate {
    readonly session: string;
    readonly public: string;
    readonly iteration: number;
    readonly private?: Record<string, string> | null;
}
export declare class SsmContext implements SsmContextDTO {
    constructor(session: string, _public: string, iteration: number, _private?: Record<string, string> | null);
    get session(): string;
    get public(): string;
    get iteration(): number;
    get private(): Record<string, string> | null;
    copy(session?: string, _public?: string, iteration?: number, _private?: Record<string, string> | null): SsmContext;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmContext {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmContext;
    }
}
export declare interface SsmGrantDTO {
    readonly user: string;
    readonly iteration: number;
    readonly credits: Record<string, CreditDTO>;
}
export declare class SsmGrant {
    constructor(user: string, iteration: number, credits: Record<string, Credit>);
    get user(): string;
    get iteration(): number;
    get credits(): Record<string, Credit>;
    copy(user?: string, iteration?: number, credits?: Record<string, Credit>): SsmGrant;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmGrant {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGrant;
    }
}
export declare interface CreditDTO {
    readonly amount: number;
}
export declare class Credit implements CreditDTO {
    constructor(amount: number);
    get amount(): number;
    copy(amount?: number): Credit;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace Credit {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Credit;
    }
}
export declare interface SsmSessionDTO extends WithPrivate {
    readonly ssm?: string | null;
    readonly session: string;
    readonly roles?: Record<string, string> | null;
    readonly public?: unknown;
    readonly private?: Record<string, string> | null;
}
export declare class SsmSession implements SsmSessionDTO {
    constructor(ssm: string, session: string, roles: Record<string, string>, _public: string, _private?: Record<string, string> | null);
    get ssm(): string;
    get session(): string;
    get roles(): Record<string, string>;
    get public(): string;
    get private(): Record<string, string> | null;
}
export declare namespace SsmSession {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmSession;
    }
}
export declare interface SsmSessionStateDTO extends SsmSessionDTO, WithPrivate {
    readonly ssm?: string | null;
    readonly session: string;
    readonly roles?: Record<string, string> | null;
    readonly public?: unknown;
    readonly private?: Record<string, string> | null;
    readonly origin?: SsmTransitionDTO | null;
    readonly current: number;
    readonly iteration: number;
}
export declare class SsmSessionState implements SsmSessionStateDTO {
    constructor(ssm: string | null, session: string, roles: Record<string, string> | null, _public: unknown, _private: Record<string, string> | null | undefined, origin: SsmTransition | null, current: number, iteration: number);
    get ssm(): string | null;
    get session(): string;
    get roles(): Record<string, string> | null;
    get public(): unknown;
    get private(): Record<string, string> | null;
    get origin(): SsmTransition | null;
    get current(): number;
    get iteration(): number;
    copy(ssm?: string | null, session?: string, roles?: Record<string, string> | null, _public?: unknown, _private?: Record<string, string> | null, origin?: SsmTransition | null, current?: number, iteration?: number): SsmSessionState;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmSessionState {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmSessionState;
    }
}
export declare interface SsmSessionStateLogDTO {
    readonly txId: string;
    readonly state: SsmSessionStateDTO;
}
export declare class SsmSessionStateLog implements SsmSessionStateLogDTO {
    constructor(txId: string, state: SsmSessionState);
    get txId(): string;
    get state(): SsmSessionState;
    copy(txId?: string, state?: SsmSessionState): SsmSessionStateLog;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmSessionStateLog {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmSessionStateLog;
    }
}
export declare interface SsmTransitionDTO {
    readonly from: number;
    readonly to: number;
    readonly role: string;
    readonly action: string;
}
export declare class SsmTransition implements SsmTransitionDTO {
    constructor(from: number, to: number, role: string, action: string);
    get from(): number;
    get to(): number;
    get role(): string;
    get action(): string;
    copy(from?: number, to?: number, role?: string, action?: string): SsmTransition;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmTransition {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmTransition;
    }
}
export declare interface WithPrivate {
    readonly private?: Record<string, string> | null;
}
export declare interface SsmUriDTO {
    readonly uri: string;
}
export declare class SsmUri implements SsmUriDTO {
    constructor(uri: string);
    get uri(): string;
    get channelId(): string;
    get chaincodeId(): string;
    get ssmName(): string;
    get ssmVersion(): string;
    get chaincodeUri(): ChaincodeUri;
    copy(uri?: string): SsmUri;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmUri {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmUri;
    }
    abstract class Companion extends KtSingleton<Companion.$metadata$.constructor>() {
        private constructor();
    }
    namespace Companion {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor {
                get PARTS(): number;
                get PREFIX(): string;
                private constructor();
            }
        }
    }
}
export declare class SsmGetAdminQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, name: string);
    get chaincodeUri(): ChaincodeUri;
    get name(): string;
}
export declare namespace SsmGetAdminQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetAdminQuery;
    }
}
export declare class SsmGetAdminResult implements SsmItemResultDTO<Agent> {
    constructor(item?: Agent | null);
    get item(): Agent | null;
}
export declare namespace SsmGetAdminResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetAdminResult;
    }
}
export declare class SsmGetQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, name: string);
    get chaincodeUri(): ChaincodeUri;
    get name(): string;
}
export declare namespace SsmGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetQuery;
    }
}
export declare class SsmGetResult implements SsmItemResultDTO<Ssm> {
    constructor(item?: Ssm | null);
    get item(): Ssm | null;
}
export declare namespace SsmGetResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetResult;
    }
}
export declare class SsmGetSessionLogsQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, ssmName: string, sessionName: string);
    get chaincodeUri(): ChaincodeUri;
    get ssmName(): string;
    get sessionName(): string;
}
export declare namespace SsmGetSessionLogsQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionLogsQuery;
    }
}
export declare class SsmGetSessionLogsQueryResult {
    constructor(ssmName: string, sessionName: string, logs: SsmSessionStateLog[]);
    get ssmName(): string;
    get sessionName(): string;
    get logs(): SsmSessionStateLog[];
    copy(ssmName?: string, sessionName?: string, logs?: SsmSessionStateLog[]): SsmGetSessionLogsQueryResult;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmGetSessionLogsQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionLogsQueryResult;
    }
}
export declare class SsmGetSessionQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, sessionName: string);
    get chaincodeUri(): ChaincodeUri;
    get sessionName(): string;
}
export declare namespace SsmGetSessionQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionQuery;
    }
}
export declare class SsmGetSessionResult implements SsmItemResultDTO<SsmSessionState> {
    constructor(item?: SsmSessionState | null);
    get item(): SsmSessionState | null;
}
export declare namespace SsmGetSessionResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionResult;
    }
}
export declare class SsmGetTransactionQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, id: string);
    get chaincodeUri(): ChaincodeUri;
    get id(): string;
    copy(chaincodeUri?: ChaincodeUri, id?: string): SsmGetTransactionQuery;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace SsmGetTransactionQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetTransactionQuery;
    }
}
export declare class SsmGetTransactionQueryResult implements SsmItemResultDTO<Transaction> {
    constructor(item?: Transaction | null);
    get item(): Transaction | null;
}
export declare namespace SsmGetTransactionQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetTransactionQueryResult;
    }
}
export declare class SsmGetUserQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, name: string);
    get chaincodeUri(): ChaincodeUri;
    get name(): string;
}
export declare namespace SsmGetUserQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetUserQuery;
    }
}
export declare class SsmGetUserResult implements SsmItemResultDTO<Agent> {
    constructor(item?: Agent | null);
    get item(): Agent | null;
}
export declare namespace SsmGetUserResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetUserResult;
    }
}
export declare class SsmListAdminQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
}
export declare namespace SsmListAdminQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListAdminQuery;
    }
}
export declare class SsmListAdminResult implements SsmItemsResultDTO<string> {
    constructor(items: Array<string>);
    get items(): Array<string>;
}
export declare namespace SsmListAdminResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListAdminResult;
    }
}
export declare class SsmListSessionQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
}
export declare namespace SsmListSessionQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListSessionQuery;
    }
}
export declare class SsmListSessionResult implements SsmItemsResultDTO<string> {
    constructor(items: Array<string>);
    get items(): Array<string>;
}
export declare namespace SsmListSessionResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListSessionResult;
    }
}
export declare class SsmListSsmQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
}
export declare namespace SsmListSsmQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListSsmQuery;
    }
}
export declare class SsmListSsmResult implements SsmItemsResultDTO<string> {
    constructor(items: Array<string>);
    get items(): Array<string>;
}
export declare namespace SsmListSsmResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListSsmResult;
    }
}
export declare class SsmListUserQuery implements SsmQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
}
export declare namespace SsmListUserQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListUserQuery;
    }
}
export declare class SsmListUserResult implements SsmItemsResultDTO<string> {
    constructor(items: Array<string>);
    get items(): Array<string>;
}
export declare namespace SsmListUserResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListUserResult;
    }
}
export type F2ClientType = "HTTP";
export declare namespace F2ClientType {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2ClientType;
    }
}
export declare interface F2Client {
    readonly type: F2ClientType;
}
/** @deprecated  */
export declare const initHook: {
    get(): unknown;
};
export declare interface HttpClientBuilder {
    build(urlBase: string): HttpF2Client;
}
export declare class HttpF2Client implements F2Client {
    private constructor();
    get urlBase(): string;
    get type(): F2ClientType;
}
export declare namespace HttpF2Client {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => HttpF2Client;
    }
}
export declare interface F2UploadCommand<COMMAND> {
    readonly command: COMMAND;
    readonly fileMap: Record<string, F2FilePart[]>;
}
export declare class F2UploadSingleCommand<COMMAND> implements F2UploadCommand<COMMAND> {
    constructor(command: COMMAND, file: F2FilePart);
    get command(): COMMAND;
    get file(): F2FilePart;
    get fileMap(): Record<string, F2FilePart[]>;
    copy(command?: COMMAND, file?: F2FilePart): F2UploadSingleCommand<COMMAND>;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace F2UploadSingleCommand {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <COMMAND>() => F2UploadSingleCommand<COMMAND>;
    }
}
export declare class F2UploadMultipleCommand<COMMAND> implements F2UploadCommand<COMMAND> {
    constructor(command: COMMAND, files: F2FilePart[]);
    get command(): COMMAND;
    get files(): F2FilePart[];
    get fileMap(): Record<string, F2FilePart[]>;
    copy(command?: COMMAND, files?: F2FilePart[]): F2UploadMultipleCommand<COMMAND>;
    toString(): string;
    hashCode(): number;
    equals(other?: unknown): boolean;
}
export declare namespace F2UploadMultipleCommand {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <COMMAND>() => F2UploadMultipleCommand<COMMAND>;
    }
}
export declare class F2FilePart {
    private constructor();
    get name(): string;
    get contentType(): string | null;
    static fromBytes(name: string, bytes: Int8Array, contentType?: string | null): F2FilePart;
}
export declare namespace F2FilePart {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2FilePart;
    }
}
export declare function httpClientBuilderDefault(): HttpClientBuilder;
export declare function httpF2Supplier<RESPONSE>(client: HttpF2Client, route: string): F2Supplier<RESPONSE>;
export declare function httpF2Function<QUERY, RESPONSE>(client: HttpF2Client, route: string): F2Function<QUERY, RESPONSE>;
export declare function httpF2Consumer<QUERY>(client: HttpF2Client, route: string): F2Consumer<QUERY>;
export declare class InvalidUrlException extends /* IllegalArgumentException */ Error {
    constructor(url: string);
}
export declare namespace InvalidUrlException {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => InvalidUrlException;
    }
}
export declare abstract class AuthRealm {
    private constructor();
    get serverUrl(): string;
    get realmId(): string;
    get clientId(): string;
    get redirectUrl(): string | null;
}
export declare namespace AuthRealm {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => AuthRealm;
    }
}
export declare class AuthRealmPassword extends AuthRealm.$metadata$.constructor {
    constructor(serverUrl: string, realmId: string, redirectUrl: string, clientId: string, username: string, password: string);
    get serverUrl(): string;
    get realmId(): string;
    get redirectUrl(): string;
    get clientId(): string;
    get username(): string;
    get password(): string;
    toString(): string;
}
export declare namespace AuthRealmPassword {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => AuthRealmPassword;
    }
}
export declare class AuthRealmClientSecret extends AuthRealm.$metadata$.constructor {
    constructor(serverUrl: string, realmId: string, clientId: string, redirectUrl: string | null | undefined, clientSecret: string, isPublic?: boolean);
    get serverUrl(): string;
    get realmId(): string;
    get clientId(): string;
    get redirectUrl(): string | null;
    get clientSecret(): string;
    get isPublic(): boolean;
    toString(): string;
}
export declare namespace AuthRealmClientSecret {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => AuthRealmClientSecret;
    }
}