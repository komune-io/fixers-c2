/** A Kotlin value the export cannot express, such as an `Instant`: opaque to TypeScript. */
export declare interface KtOpaque<N extends string> {
    readonly __kotlinOpaque: N;
    readonly __doNotUseOrImplementIt: {
        readonly "kotlin.Opaque": unique symbol;
    };
}
/**
 * The JSON a Kotlin value of type `T` is sent as: a list or a set is an array, a map an object
 * (whose keys are optional when they are an enum's names), a `Long` a number, an enum its name,
 * an opaque value a string, and a nullable property may be absent. The exported types describe
 * the Kotlin objects; a body read from or written to the wire is a `Json<T>`, and the compiled
 * code accepts none.
 *
 * What it does not say: a sealed or polymorphic type carries no discriminant here, so a
 * serializer's `type` field is not typed; a nullable property is absent from the JSON only where
 * the serializer omits nulls (F2's `explicitNulls = false`, Jackson), a default kotlinx `Json`
 * writes `null`; and a `Long` beyond 2^53 loses its precision as a JavaScript number.
 */
export type Json<T> =
    T extends { asJsReadonlyArrayView(): ReadonlyArray<infer E> } ? Json<E>[]
    : T extends { asJsReadonlySetView(): ReadonlySet<infer E> } ? Json<E>[]
    : T extends { asJsReadonlyMapView(): ReadonlyMap<infer K, infer V> } ? JsonMap<JsonKey<K>, Json<V>>
    : T extends { readonly __kotlinOpaque: string } ? string
    : T extends bigint ? number
    : T extends string | number | boolean | null | undefined ? T
    : T extends { readonly name: infer N extends string; readonly ordinal: infer O extends number }
        ? string extends N ? JsonObject<T> : number extends O ? JsonObject<T> : N
    : T extends ReadonlyArray<infer E> ? Json<E>[]
    : T extends (...args: never[]) => unknown ? never
    : T extends object ? JsonObject<T>
    : T;
type JsonKey<K> = Json<K> extends string | number ? Json<K> : string;
// A map holds the entries it has: keys out of a finite set, such as an enum's names, are optional.
type JsonMap<K extends string | number, V> =
    string extends K ? Record<string, V> : number extends K ? Record<number, V> : { [P in K]?: V };
type JsonField<T, P extends keyof T> =
    P extends "__doNotUseOrImplementIt" ? never : T[P] extends (...args: never[]) => unknown ? never : P;
type JsonFields<T> = { [P in keyof T]-?: JsonField<T, P> }[keyof T];
type JsonOptional<T> = {
    [P in JsonFields<T>]-?: null extends T[P] ? P : undefined extends T[P] ? P : never
}[JsonFields<T>];
type JsonObject<T> =
    { [P in Exclude<JsonFields<T>, JsonOptional<T>>]: Json<T[P]> } & { [P in JsonOptional<T>]?: Json<T[P]> };
type Nullable<T> = T | null | undefined
declare function KtSingleton<T>(): T & (abstract new() => object);
export declare interface KtList<out E> /* extends Collection<E> */ {
    asJsReadonlyArrayView(): ReadonlyArray<E>;
    readonly __doNotUseOrImplementIt: {
        readonly "kotlin.collections.KtList": unique symbol;
    };
}
export declare namespace KtList {
    function fromJsArray<E>(array: ReadonlyArray<E>): KtList<E>;
}
export declare interface KtMap<K, out V> {
    asJsReadonlyMapView(): ReadonlyMap<K, V>;
    readonly __doNotUseOrImplementIt: {
        readonly "kotlin.collections.KtMap": unique symbol;
    };
}
export declare namespace KtMap {
    function fromJsMap<K, V>(map: ReadonlyMap<K, V>): KtMap<K, V>;
}
export declare interface BlockDTO {
    readonly blockId: number;
    readonly previousHash: Int8Array;
    readonly dataHash: Int8Array;
    readonly transactions: KtList<TransactionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "io.komune.c2.chaincode.dsl.BlockDTO": unique symbol;
    };
}
export declare class Block implements BlockDTO {
    constructor(blockId: number, previousHash: Int8Array, dataHash: Int8Array, transactions: KtList<Transaction>);
    get blockId(): number;
    get previousHash(): Int8Array;
    get dataHash(): Int8Array;
    get transactions(): KtList<Transaction>;
    readonly __doNotUseOrImplementIt: BlockDTO["__doNotUseOrImplementIt"];
}
export declare namespace Block {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Block;
    }
}
export declare interface ChaincodeUriDTO {
    readonly uri: string;
    readonly __doNotUseOrImplementIt: {
        readonly "io.komune.c2.chaincode.dsl.ChaincodeUriDTO": unique symbol;
    };
}
export declare class ChaincodeUri implements ChaincodeUriDTO {
    constructor(uri: string);
    get uri(): string;
    get channelId(): string;
    get chaincodeId(): string;
    copy(uri?: string): ChaincodeUri;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: ChaincodeUriDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: {
        readonly "io.komune.c2.chaincode.dsl.IdentitiesInfoDTO": unique symbol;
    };
}
export declare class IdentitiesInfo implements IdentitiesInfoDTO {
    constructor(id: string, mspid: string);
    get id(): string;
    get mspid(): string;
    readonly __doNotUseOrImplementIt: IdentitiesInfoDTO["__doNotUseOrImplementIt"];
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
    readonly timestamp: bigint;
    readonly isValid: boolean;
    readonly channelId: string;
    readonly creator: IdentitiesInfoDTO;
    readonly nonce: Int8Array;
    readonly __doNotUseOrImplementIt: {
        readonly "io.komune.c2.chaincode.dsl.TransactionDTO": unique symbol;
    };
}
export declare class Transaction implements TransactionDTO {
    constructor(transactionId: string, blockId: number, timestamp: bigint, isValid: boolean, channelId: string, creator: IdentitiesInfo, nonce: Int8Array);
    get transactionId(): string;
    get blockId(): number;
    get timestamp(): bigint;
    get isValid(): boolean;
    get channelId(): string;
    get creator(): IdentitiesInfo;
    get nonce(): Int8Array;
    readonly __doNotUseOrImplementIt: TransactionDTO["__doNotUseOrImplementIt"];
}
export declare namespace Transaction {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Transaction;
    }
}
export declare interface F2Consumer<T> {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.fnc.F2Consumer": unique symbol;
    };
}
export declare interface F2Function<in T, out R> {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.fnc.F2Function": unique symbol;
    };
}
export declare interface F2Supplier<R> {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.fnc.F2Supplier": unique symbol;
    };
}
export declare interface F2SupplierSingle<R> {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.fnc.F2SupplierSingle": unique symbol;
    };
}
export declare class Batch {
    constructor(size?: number, concurrency?: number);
    get size(): number;
    get concurrency(): number;
    copy(size?: number, concurrency?: number): Batch;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
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
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.Command": unique symbol;
    } & Message["__doNotUseOrImplementIt"];
}
export declare interface Event extends Message {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.Event": unique symbol;
    } & Message["__doNotUseOrImplementIt"];
}
export declare interface Message {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.Message": unique symbol;
    };
}
export declare interface Problem extends Message {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.Problem": unique symbol;
    } & Message["__doNotUseOrImplementIt"];
}
export declare interface Query extends Message {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.Query": unique symbol;
    } & Message["__doNotUseOrImplementIt"];
}
export declare interface EnvelopeDTO<T> extends WithEnvelopeId, WithEnvelopeData<T>, Message {
    readonly id: string;
    readonly data: T;
    readonly type: string;
    readonly datacontenttype: Nullable<string>;
    readonly specversion: Nullable<string>;
    readonly source: Nullable<string>;
    readonly time: Nullable<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.envelope.EnvelopeDTO": unique symbol;
    } & Message["__doNotUseOrImplementIt"] & WithEnvelopeData<unknown>["__doNotUseOrImplementIt"] & WithEnvelopeId["__doNotUseOrImplementIt"];
}
export declare class Envelope<T> implements EnvelopeDTO<T> {
    constructor(id: string, data: T, type: string, datacontenttype?: Nullable<string>, source?: Nullable<string>, time?: Nullable<string>);
    get id(): string;
    get data(): T;
    get type(): string;
    get datacontenttype(): Nullable<string>;
    get source(): Nullable<string>;
    get time(): Nullable<string>;
    get specversion(): string;
    copy(id?: string, data?: T, type?: string, datacontenttype?: Nullable<string>, source?: Nullable<string>, time?: Nullable<string>): Envelope<T>;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: EnvelopeDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare namespace Envelope {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <T>() => Envelope<T>;
    }
}
export declare interface WithEnvelopeData<T> {
    readonly data: T;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.envelope.WithEnvelopeData": unique symbol;
    };
}
export declare interface WithEnvelopeId {
    readonly id: string;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.envelope.WithEnvelopeId": unique symbol;
    };
}
export declare interface F2ErrorDTO extends Problem {
    readonly id: Nullable<string>;
    readonly timestamp: string;
    readonly code: number;
    readonly requestId: Nullable<string>;
    readonly message: string;
    readonly refusal: Nullable<F2RefusalDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.error.F2ErrorDTO": unique symbol;
    } & Problem["__doNotUseOrImplementIt"];
}
export declare interface F2RefusalDTO {
    readonly code: string;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.error.F2RefusalDTO": unique symbol;
    };
}
export declare class F2Refusal implements F2RefusalDTO {
    constructor(code: string, parameters?: KtMap<string, string>, citers?: KtList<string>);
    get code(): string;
    copy(code?: string, parameters?: KtMap<string, string>, citers?: KtList<string>): F2Refusal;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: F2RefusalDTO["__doNotUseOrImplementIt"];
}
export declare namespace F2Refusal {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => F2Refusal;
    }
}
export declare class F2Error implements F2ErrorDTO {
    constructor(message: string, id?: Nullable<string>, timestamp?: string, code?: number, requestId?: Nullable<string>, refusal?: Nullable<F2Refusal>);
    get message(): string;
    get id(): Nullable<string>;
    get timestamp(): string;
    get code(): number;
    get requestId(): Nullable<string>;
    get refusal(): Nullable<F2Refusal>;
    toString(): string;
    readonly __doNotUseOrImplementIt: F2ErrorDTO["__doNotUseOrImplementIt"];
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
    constructor(error: F2ErrorDTO, cause?: Nullable<Error>);
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
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.filter.Match": unique symbol;
    };
}
export declare interface SortDTO {
    readonly property: string;
    readonly ascending: boolean;
    readonly nullsFirst: Nullable<boolean>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.filter.SortDTO": unique symbol;
    };
}
export declare interface PageDTO<out OBJECT> {
    readonly total: number;
    readonly items: KtList<OBJECT>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.PageDTO": unique symbol;
    };
}
export declare class Page<out OBJECT> implements PageDTO<OBJECT> {
    constructor(total: number, items: KtList<OBJECT>);
    get total(): number;
    get items(): KtList<OBJECT>;
    copy(total?: number, items?: KtList<OBJECT>): Page<OBJECT>;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: PageDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare namespace Page {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <OBJECT>() => Page<OBJECT>;
    }
}
export declare interface PageQueryDTO extends Query {
    readonly pagination: Nullable<OffsetPaginationDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.PageQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface PageQueryResultDTO<out OBJECT> extends Event, PageDTO<OBJECT> {
    readonly total: number;
    readonly items: KtList<OBJECT>;
    readonly pagination: Nullable<OffsetPaginationDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.PageQueryResultDTO": unique symbol;
    } & PageDTO<unknown>["__doNotUseOrImplementIt"] & Event["__doNotUseOrImplementIt"];
}
export declare class PageQuery implements PageQueryDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO>);
    get pagination(): Nullable<OffsetPaginationDTO>;
    copy(pagination?: Nullable<OffsetPaginationDTO>): PageQuery;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: PageQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace PageQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => PageQuery;
    }
}
export declare class PageQueryResult<out OBJECT> implements PageQueryResultDTO<OBJECT> {
    constructor(pagination: Nullable<OffsetPagination>, total: number, items: KtList<OBJECT>);
    get pagination(): Nullable<OffsetPagination>;
    get total(): number;
    get items(): KtList<OBJECT>;
    copy(pagination?: Nullable<OffsetPagination>, total?: number, items?: KtList<OBJECT>): PageQueryResult<OBJECT>;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: PageQueryResultDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare namespace PageQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <OBJECT>() => PageQueryResult<OBJECT>;
    }
}
export declare interface Pagination {
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.Pagination": unique symbol;
    };
}
export declare interface OffsetPaginationDTO extends Pagination {
    readonly offset: number;
    readonly limit: number;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.OffsetPaginationDTO": unique symbol;
    } & Pagination["__doNotUseOrImplementIt"];
}
export declare interface PagePaginationDTO extends Pagination {
    readonly page: Nullable<number>;
    readonly size: Nullable<number>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.page.PagePaginationDTO": unique symbol;
    } & Pagination["__doNotUseOrImplementIt"];
}
export declare class OffsetPagination implements OffsetPaginationDTO {
    constructor(offset: number, limit: number);
    get offset(): number;
    get limit(): number;
    copy(offset?: number, limit?: number): OffsetPagination;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: OffsetPaginationDTO["__doNotUseOrImplementIt"];
}
export declare namespace OffsetPagination {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => OffsetPagination;
    }
}
export declare class PagePagination implements PagePaginationDTO {
    constructor(page: Nullable<number>, size: Nullable<number>);
    get page(): Nullable<number>;
    get size(): Nullable<number>;
    copy(page?: Nullable<number>, size?: Nullable<number>): PagePagination;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: PagePaginationDTO["__doNotUseOrImplementIt"];
}
export declare namespace PagePagination {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => PagePagination;
    }
}
export declare function refusalParameters(refusal: F2RefusalDTO): ReadonlyMap<string, string>;
export declare function refusalCiters(refusal: F2RefusalDTO): ReadonlyArray<string>;
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
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.SsmChaincodeQueries": unique symbol;
    };
}
export declare interface SsmQueryDTO extends Query {
    readonly chaincodeUri: ChaincodeUri;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.SsmQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface SsmItemResultDTO<T> extends Event {
    readonly item: Nullable<T>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.SsmItemResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare interface SsmItemsResultDTO<T> extends Event {
    readonly items: Array<T>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.SsmItemsResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare interface BatchPropertiesDTO {
    readonly timeout: number;
    readonly size: number;
    readonly concurrency: number;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.config.BatchPropertiesDTO": unique symbol;
    };
}
export declare class SsmBatchProperties implements BatchPropertiesDTO {
    constructor(timeout?: number, size?: number, concurrency?: number);
    get timeout(): number;
    get size(): number;
    get concurrency(): number;
    readonly __doNotUseOrImplementIt: BatchPropertiesDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmBatchProperties {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmBatchProperties;
    }
}
export declare interface SsmChaincodePropertiesDTO {
    readonly url: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.config.SsmChaincodePropertiesDTO": unique symbol;
    };
}
export declare class ChaincodeSsmConfig implements SsmChaincodePropertiesDTO {
    constructor(url: string);
    get url(): string;
    readonly __doNotUseOrImplementIt: SsmChaincodePropertiesDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.AgentDTO": unique symbol;
    };
}
export declare class Agent implements AgentDTO {
    constructor(name: string, pub: Int8Array);
    get name(): string;
    get pub(): Int8Array;
    equals(other: Nullable<unknown>): boolean;
    hashCode(): number;
    copy(name?: string, pub?: Int8Array): Agent;
    toString(): string;
    readonly __doNotUseOrImplementIt: AgentDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.ChaincodeDTO": unique symbol;
    };
}
export declare class Chaincode implements ChaincodeDTO {
    constructor(id: string, channelId: string);
    get id(): string;
    get channelId(): string;
    copy(id?: string, channelId?: string): Chaincode;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: ChaincodeDTO["__doNotUseOrImplementIt"];
}
export declare namespace Chaincode {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Chaincode;
    }
}
export declare interface SsmDTO {
    readonly name: string;
    readonly transitions: KtList<SsmTransitionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmDTO": unique symbol;
    };
}
export declare class Ssm implements SsmDTO {
    constructor(name: string, transitions: KtList<SsmTransition>);
    get name(): string;
    get transitions(): KtList<SsmTransition>;
    copy(name?: string, transitions?: KtList<SsmTransition>): Ssm;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmDTO["__doNotUseOrImplementIt"];
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
    readonly private: Nullable<KtMap<string, string>>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmContextDTO": unique symbol;
    } & WithPrivate["__doNotUseOrImplementIt"];
}
export declare class SsmContext implements SsmContextDTO {
    constructor(session: string, _public: string, iteration: number, _private?: Nullable<KtMap<string, string>>);
    get session(): string;
    get public(): string;
    get iteration(): number;
    get private(): Nullable<KtMap<string, string>>;
    copy(session?: string, _public?: string, iteration?: number, _private?: Nullable<KtMap<string, string>>): SsmContext;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmContextDTO["__doNotUseOrImplementIt"];
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
    readonly credits: KtMap<string, CreditDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmGrantDTO": unique symbol;
    };
}
export declare class SsmGrant {
    constructor(user: string, iteration: number, credits: KtMap<string, Credit>);
    get user(): string;
    get iteration(): number;
    get credits(): KtMap<string, Credit>;
    copy(user?: string, iteration?: number, credits?: KtMap<string, Credit>): SsmGrant;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
}
export declare namespace SsmGrant {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGrant;
    }
}
export declare interface CreditDTO {
    readonly amount: number;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.CreditDTO": unique symbol;
    };
}
export declare class Credit implements CreditDTO {
    constructor(amount: number);
    get amount(): number;
    copy(amount?: number): Credit;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: CreditDTO["__doNotUseOrImplementIt"];
}
export declare namespace Credit {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Credit;
    }
}
export declare interface SsmSessionDTO extends WithPrivate {
    readonly ssm: Nullable<string>;
    readonly session: string;
    readonly roles: Nullable<KtMap<string, string>>;
    readonly public: Nullable<unknown>;
    readonly private: Nullable<KtMap<string, string>>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmSessionDTO": unique symbol;
    } & WithPrivate["__doNotUseOrImplementIt"];
}
export declare class SsmSession implements SsmSessionDTO {
    constructor(ssm: string, session: string, roles: KtMap<string, string>, _public: string, _private?: Nullable<KtMap<string, string>>);
    get ssm(): string;
    get session(): string;
    get roles(): KtMap<string, string>;
    get public(): string;
    get private(): Nullable<KtMap<string, string>>;
    readonly __doNotUseOrImplementIt: SsmSessionDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmSession {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmSession;
    }
}
export declare interface SsmSessionStateDTO extends SsmSessionDTO, WithPrivate {
    readonly ssm: Nullable<string>;
    readonly session: string;
    readonly roles: Nullable<KtMap<string, string>>;
    readonly public: Nullable<unknown>;
    readonly private: Nullable<KtMap<string, string>>;
    readonly origin: Nullable<SsmTransitionDTO>;
    readonly current: number;
    readonly iteration: number;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmSessionStateDTO": unique symbol;
    } & WithPrivate["__doNotUseOrImplementIt"] & SsmSessionDTO["__doNotUseOrImplementIt"];
}
export declare class SsmSessionState implements SsmSessionStateDTO {
    constructor(ssm: Nullable<string>, session: string, roles: Nullable<KtMap<string, string>>, _public: Nullable<unknown>, _private: Nullable<KtMap<string, string>> | undefined, origin: Nullable<SsmTransition>, current: number, iteration: number);
    get ssm(): Nullable<string>;
    get session(): string;
    get roles(): Nullable<KtMap<string, string>>;
    get public(): Nullable<unknown>;
    get private(): Nullable<KtMap<string, string>>;
    get origin(): Nullable<SsmTransition>;
    get current(): number;
    get iteration(): number;
    copy(ssm?: Nullable<string>, session?: string, roles?: Nullable<KtMap<string, string>>, _public?: Nullable<unknown>, _private?: Nullable<KtMap<string, string>>, origin?: Nullable<SsmTransition>, current?: number, iteration?: number): SsmSessionState;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmSessionStateDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmSessionStateLogDTO": unique symbol;
    };
}
export declare class SsmSessionStateLog implements SsmSessionStateLogDTO {
    constructor(txId: string, state: SsmSessionState);
    get txId(): string;
    get state(): SsmSessionState;
    copy(txId?: string, state?: SsmSessionState): SsmSessionStateLog;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmSessionStateLogDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.SsmTransitionDTO": unique symbol;
    };
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
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmTransitionDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmTransition {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmTransition;
    }
}
export declare interface WithPrivate {
    readonly private: Nullable<KtMap<string, string>>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.WithPrivate": unique symbol;
    };
}
export declare interface SsmUriDTO {
    readonly uri: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.chaincode.dsl.model.uri.SsmUriDTO": unique symbol;
    };
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
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmUriDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetAdminQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetAdminQuery;
    }
}
export declare class SsmGetAdminResult implements SsmItemResultDTO<Agent> {
    constructor(item: Nullable<Agent>);
    get item(): Nullable<Agent>;
    readonly __doNotUseOrImplementIt: SsmItemResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetQuery;
    }
}
export declare class SsmGetResult implements SsmItemResultDTO<Ssm> {
    constructor(item: Nullable<Ssm>);
    get item(): Nullable<Ssm>;
    readonly __doNotUseOrImplementIt: SsmItemResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetSessionLogsQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionLogsQuery;
    }
}
export declare class SsmGetSessionLogsQueryResult {
    constructor(ssmName: string, sessionName: string, logs: KtList<SsmSessionStateLog>);
    get ssmName(): string;
    get sessionName(): string;
    get logs(): KtList<SsmSessionStateLog>;
    copy(ssmName?: string, sessionName?: string, logs?: KtList<SsmSessionStateLog>): SsmGetSessionLogsQueryResult;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<unknown>): boolean;
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetSessionQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetSessionQuery;
    }
}
export declare class SsmGetSessionResult implements SsmItemResultDTO<SsmSessionState> {
    constructor(item: Nullable<SsmSessionState>);
    get item(): Nullable<SsmSessionState>;
    readonly __doNotUseOrImplementIt: SsmItemResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    equals(other: Nullable<unknown>): boolean;
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetTransactionQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetTransactionQuery;
    }
}
export declare class SsmGetTransactionQueryResult implements SsmItemResultDTO<Transaction> {
    constructor(item: Nullable<Transaction>);
    get item(): Nullable<Transaction>;
    readonly __doNotUseOrImplementIt: SsmItemResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmGetUserQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmGetUserQuery;
    }
}
export declare class SsmGetUserResult implements SsmItemResultDTO<Agent> {
    constructor(item: Nullable<Agent>);
    get item(): Nullable<Agent>;
    readonly __doNotUseOrImplementIt: SsmItemResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmItemsResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmItemsResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmItemsResultDTO<unknown>["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmQueryDTO["__doNotUseOrImplementIt"];
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
    readonly __doNotUseOrImplementIt: SsmItemsResultDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare namespace SsmListUserResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmListUserResult;
    }
}