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
 * code accepts none: an instance of an exported class that implements an exported interface, or
 * of a data class, is refused at any depth.
 *
 * What it does not say: a sealed or polymorphic type carries no discriminant here, so a
 * serializer's `type` field is not typed. A nullable property may be absent: F2's Kotlin client
 * omits nulls (`explicitNulls = false`), Jackson and a default kotlinx `Json` write `null`. A
 * reader that requires the property refuses the JSON that omits it: a default kotlinx `Json`
 * does, Jackson and F2's `Json` read it as `null`. An instance of a class that is neither of the
 * two above is not told apart from a plain object, and a `Long` beyond 2^53 loses its precision
 * as a JavaScript number.
 *
 * These types require `strictNullChecks`: without it, `null` extends every property's type, so
 * every property of a `Json<T>` is optional.
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
// What a Kotlin object has and JSON never does: the brand of an exported interface, the members of a data class.
type JsonObject<T> =
    { [P in Exclude<JsonFields<T>, JsonOptional<T>>]: Json<T[P]> } & { [P in JsonOptional<T>]?: Json<T[P]> }
    & { readonly [P in "__doNotUseOrImplementIt" | Exclude<"equals" | "hashCode", JsonFields<T>>]?: never };
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
    readonly parameters: KtMap<string, string>;
    readonly citers: KtList<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "f2.dsl.cqrs.error.F2RefusalDTO": unique symbol;
    };
}
export declare class F2Refusal implements F2RefusalDTO {
    constructor(code: string, parameters?: KtMap<string, string>, citers?: KtList<string>);
    get code(): string;
    get parameters(): KtMap<string, string>;
    get citers(): KtList<string>;
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
export declare interface Automate {
    getAvailableTransitions(state: S2State): Array<S2Transition>;
    isAvailableTransition(currentState: S2State, msg: Message): boolean;
    isAvailableInitTransition(command: Message): boolean;
    isFinalState(state: S2State): boolean;
    isSameState(from: Nullable<S2State>, to: S2State): boolean;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.Automate": unique symbol;
    };
}
export declare abstract class ErrorCategory {
    private constructor();
    static get Rejected(): ErrorCategory & {
        get name(): "Rejected";
        get ordinal(): 0;
    };
    static get Transient(): ErrorCategory & {
        get name(): "Transient";
        get ordinal(): 1;
    };
    static get Indeterminate(): ErrorCategory & {
        get name(): "Indeterminate";
        get ordinal(): 2;
    };
    static get Conflict(): ErrorCategory & {
        get name(): "Conflict";
        get ordinal(): 3;
    };
    static values(): [typeof ErrorCategory.Rejected, typeof ErrorCategory.Transient, typeof ErrorCategory.Indeterminate, typeof ErrorCategory.Conflict];
    static valueOf(value: string): ErrorCategory;
    get name(): "Rejected" | "Transient" | "Indeterminate" | "Conflict";
    get ordinal(): 0 | 1 | 2 | 3;
}
export declare namespace ErrorCategory {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => ErrorCategory;
    }
}
export declare class S2Automate implements Automate {
    constructor(name: string, version: Nullable<string>, transitions: Array<S2Transition>);
    get name(): string;
    get version(): Nullable<string>;
    get transitions(): Array<S2Transition>;
    get withResultAsAction(): boolean;
    getAvailableTransitions(state: S2State): Array<S2Transition>;
    isAvailableTransition(currentState: S2State, msg: Message): boolean;
    isAvailableInitTransition(command: Message): boolean;
    isFinalState(state: S2State): boolean;
    isSameState(from: Nullable<S2State>, to: S2State): boolean;
    readonly __doNotUseOrImplementIt: Automate["__doNotUseOrImplementIt"];
}
export declare namespace S2Automate {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2Automate;
    }
}
export declare interface S2InitCommand extends Command {
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2InitCommand": unique symbol;
    } & Command["__doNotUseOrImplementIt"];
}
export declare interface S2Command<ID extends unknown> extends Command, WithId<ID> {
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2Command": unique symbol;
    } & WithId<unknown>["__doNotUseOrImplementIt"] & Command["__doNotUseOrImplementIt"];
}
export declare interface S2Error {
    readonly type: string;
    readonly description: string;
    readonly date: string;
    readonly payload: KtMap<string, string>;
    readonly cause: Nullable<Error>;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2Error": unique symbol;
    };
}
export declare class S2ErrorBase implements S2Error {
    constructor(type: string, description: string, date: string, payload: KtMap<string, string>, cause?: Nullable<Error>);
    get type(): string;
    get description(): string;
    get date(): string;
    get payload(): KtMap<string, string>;
    get cause(): Nullable<Error>;
    toString(): string;
    readonly __doNotUseOrImplementIt: S2Error["__doNotUseOrImplementIt"];
}
export declare namespace S2ErrorBase {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2ErrorBase;
    }
}
export declare interface S2Event<out STATE extends S2State, ID extends unknown> extends Event, WithId<ID> {
    readonly id: ID;
    readonly type: STATE;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2Event": unique symbol;
    } & WithId<unknown>["__doNotUseOrImplementIt"] & Event["__doNotUseOrImplementIt"];
}
export declare class S2EventSuccess<out STATE extends S2State, out COMMAND extends Command, ID extends unknown> implements Event {
    constructor(id: ID, type: COMMAND, from: STATE, to: STATE);
    get id(): ID;
    get type(): COMMAND;
    get from(): STATE;
    get to(): STATE;
    readonly __doNotUseOrImplementIt: Event["__doNotUseOrImplementIt"];
}
export declare namespace S2EventSuccess {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <STATE extends S2State, COMMAND extends Command, ID extends unknown>() => S2EventSuccess<STATE, COMMAND, ID>;
    }
}
export declare class S2EventError<out STATE extends S2State, out COMMAND extends Command, ID extends unknown> implements Event {
    constructor(id: ID, type: COMMAND, from: STATE, to: STATE, error: S2Error);
    get id(): ID;
    get type(): COMMAND;
    get from(): STATE;
    get to(): STATE;
    get error(): S2Error;
    readonly __doNotUseOrImplementIt: Event["__doNotUseOrImplementIt"];
}
export declare namespace S2EventError {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <STATE extends S2State, COMMAND extends Command, ID extends unknown>() => S2EventError<STATE, COMMAND, ID>;
    }
}
export declare interface S2Role {
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2Role": unique symbol;
    };
}
export declare interface S2State {
    readonly position: number;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.S2State": unique symbol;
    };
}
export declare class S2InitTransition {
    constructor(to: S2StateValue, role: S2Role, action: S2TransitionValue);
    get to(): S2StateValue;
    get role(): S2Role;
    get action(): S2TransitionValue;
}
export declare namespace S2InitTransition {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2InitTransition;
    }
}
export declare class S2Transition {
    constructor(from: Nullable<S2StateValue>, to: S2StateValue, role: S2RoleValue, action: S2TransitionValue, result: Nullable<S2TransitionValue>);
    get from(): Nullable<S2StateValue>;
    get to(): S2StateValue;
    get role(): S2RoleValue;
    get action(): S2TransitionValue;
    get result(): Nullable<S2TransitionValue>;
}
export declare namespace S2Transition {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2Transition;
    }
}
export declare class S2TransitionValue {
    constructor(name: string);
    get name(): string;
}
export declare namespace S2TransitionValue {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2TransitionValue;
    }
}
export declare class S2RoleValue {
    constructor(name: string);
    get name(): string;
}
export declare namespace S2RoleValue {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2RoleValue;
    }
}
export declare class S2StateValue {
    constructor(name: string, position: number);
    get name(): string;
    get position(): number;
}
export declare namespace S2StateValue {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => S2StateValue;
    }
}
export declare interface WithId<ID extends unknown> {
    readonly id: ID;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.WithId": unique symbol;
    };
}
export declare function isAvailableTransitionByName(_this_: S2Automate, stateName: Nullable<string>, type: string): boolean;
export declare function s2StateName(status: Nullable<unknown>): Nullable<string>;
export declare interface WithS2Id<ID extends unknown> {
    s2Id(): ID;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.model.WithS2Id": unique symbol;
    };
}
export declare interface WithS2IdAndStatus<ID extends unknown, STATE> extends WithS2Id<ID>, WithS2State<STATE> {
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.model.WithS2IdAndStatus": unique symbol;
    } & WithS2State<unknown>["__doNotUseOrImplementIt"] & WithS2Id<unknown>["__doNotUseOrImplementIt"];
}
export declare interface WithS2Iteration {
    s2Iteration(): number;
    withS2Iteration(iteration: number): WithS2Iteration;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.model.WithS2Iteration": unique symbol;
    };
}
export declare interface WithS2State<STATE> {
    s2State(): STATE;
    readonly __doNotUseOrImplementIt: {
        readonly "s2.dsl.automate.model.WithS2State": unique symbol;
    };
}