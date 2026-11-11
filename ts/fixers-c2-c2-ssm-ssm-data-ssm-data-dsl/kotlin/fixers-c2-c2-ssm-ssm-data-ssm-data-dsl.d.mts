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
export declare interface DatabaseDTO {
    readonly name: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.model.DatabaseDTO": unique symbol;
    };
}
export declare class Database implements DatabaseDTO {
    constructor(name: string);
    get name(): string;
    readonly __doNotUseOrImplementIt: DatabaseDTO["__doNotUseOrImplementIt"];
}
export declare namespace Database {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => Database;
    }
}
export declare interface DatabaseChangesDTO {
    readonly changeEventId: string;
    readonly docType: Nullable<DocType<unknown>>;
    readonly objectId: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.model.DatabaseChangesDTO": unique symbol;
    };
}
export declare class DatabaseChanges implements DatabaseChangesDTO {
    constructor(changeEventId: string, docType: Nullable<DocType<unknown>>, objectId: string);
    get changeEventId(): string;
    get docType(): Nullable<DocType<unknown>>;
    get objectId(): string;
    readonly __doNotUseOrImplementIt: DatabaseChangesDTO["__doNotUseOrImplementIt"];
}
export declare namespace DatabaseChanges {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DatabaseChanges;
    }
}
export declare abstract class DocType<out T extends unknown> {
    private constructor();
    get name(): string;
}
export declare namespace DocType {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new <T extends unknown>() => DocType<T>;
    }
    abstract class Admin extends KtSingleton<Admin.$metadata$.constructor>() {
        private constructor();
    }
    namespace Admin {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<Agent> {
                private constructor();
            }
        }
    }
    abstract class User extends KtSingleton<User.$metadata$.constructor>() {
        private constructor();
    }
    namespace User {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<Agent> {
                private constructor();
            }
        }
    }
    abstract class Grant extends KtSingleton<Grant.$metadata$.constructor>() {
        private constructor();
    }
    namespace Grant {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<SsmGrant> {
                private constructor();
            }
        }
    }
    abstract class Ssm extends KtSingleton<Ssm.$metadata$.constructor>() {
        private constructor();
    }
    namespace Ssm {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<Ssm> {
                private constructor();
            }
        }
    }
    abstract class State extends KtSingleton<State.$metadata$.constructor>() {
        private constructor();
    }
    namespace State {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<SsmSessionState> {
                private constructor();
            }
        }
    }
    abstract class Chaincode extends KtSingleton<Chaincode.$metadata$.constructor>() {
        private constructor();
    }
    namespace Chaincode {
        /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
        namespace $metadata$ {
            abstract class constructor extends DocType.$metadata$.constructor<ChaincodeLscc> {
                private constructor();
            }
        }
    }
}
export declare class ChaincodeLscc {
    constructor(_id: string, _rev: string);
    get _id(): string;
    get _rev(): string;
}
export declare namespace ChaincodeLscc {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => ChaincodeLscc;
    }
}
export declare interface CouchdbAdminListQueryDTO extends Query {
    readonly chaincodeUri: ChaincodeUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbAdminListQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbAdminListQueryResultDTO extends Event {
    readonly items: KtList<AgentDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbAdminListQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbAdminListQuery implements CouchdbAdminListQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
    readonly __doNotUseOrImplementIt: CouchdbAdminListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbAdminListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbAdminListQuery;
    }
}
export declare class CouchdbAdminListQueryResult implements CouchdbAdminListQueryResultDTO {
    constructor(items: KtList<Agent>);
    get items(): KtList<Agent>;
    readonly __doNotUseOrImplementIt: CouchdbAdminListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbAdminListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbAdminListQueryResult;
    }
}
export declare interface CouchdbChaincodeListQueryDTO extends Query {
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbChaincodeListQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbChaincodeListQueryResultDTO extends Event {
    readonly items: KtList<ChaincodeUriDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbChaincodeListQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbChaincodeListQuery implements CouchdbChaincodeListQueryDTO {
    constructor();
    readonly __doNotUseOrImplementIt: CouchdbChaincodeListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbChaincodeListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbChaincodeListQuery;
    }
}
export declare class CouchdbChaincodeListQueryResult implements CouchdbChaincodeListQueryResultDTO {
    constructor(items: KtList<ChaincodeUri>);
    get items(): KtList<ChaincodeUri>;
    readonly __doNotUseOrImplementIt: CouchdbChaincodeListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbChaincodeListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbChaincodeListQueryResult;
    }
}
export declare interface CouchdbDatabaseGetChangesQueryDTO extends Query {
    readonly channelId: string;
    readonly chaincodeId: string;
    readonly ssmName: string;
    readonly sessionName: Nullable<string>;
    readonly lastEventId: Nullable<string>;
    readonly limit: Nullable<bigint>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbDatabaseGetChangesQueryResultDTO extends Event {
    readonly items: KtList<DatabaseChangesDTO>;
    readonly lastEventId: Nullable<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseGetChangesQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbDatabaseGetChangesQuery implements CouchdbDatabaseGetChangesQueryDTO {
    constructor(channelId: string, chaincodeId: string, lastEventId: Nullable<string>, ssmName: string, sessionName: Nullable<string>, limit: Nullable<bigint>);
    get channelId(): string;
    get chaincodeId(): string;
    get lastEventId(): Nullable<string>;
    get ssmName(): string;
    get sessionName(): Nullable<string>;
    get limit(): Nullable<bigint>;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseGetChangesQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseGetChangesQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseGetChangesQuery;
    }
}
export declare class CouchdbDatabaseGetChangesQueryResult implements CouchdbDatabaseGetChangesQueryResultDTO {
    constructor(items: KtList<DatabaseChanges>, lastEventId: Nullable<string>);
    get items(): KtList<DatabaseChanges>;
    get lastEventId(): Nullable<string>;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseGetChangesQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseGetChangesQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseGetChangesQueryResult;
    }
}
export declare interface CouchdbDatabaseGetQueryDTO extends Query {
    readonly channelId: string;
    readonly chaincodeId: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbDatabaseGetQueryResultDTO extends Event {
    readonly item: Nullable<DatabaseDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseGetQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbDatabaseGetQuery implements CouchdbDatabaseGetQueryDTO {
    constructor(channelId: string, chaincodeId: string);
    get channelId(): string;
    get chaincodeId(): string;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseGetQuery;
    }
}
export declare class CouchdbDatabaseGetQueryResult implements CouchdbDatabaseGetQueryResultDTO {
    constructor(item: Nullable<Database>);
    get item(): Nullable<Database>;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseGetQueryResult;
    }
}
export declare interface CouchdbDatabaseListQueryDTO extends PageQueryDTO {
    readonly channelId: Nullable<string>;
    readonly chaincodeId: Nullable<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseListQueryDTO": unique symbol;
    } & PageQueryDTO["__doNotUseOrImplementIt"];
}
export declare interface CouchdbDatabaseListQueryResultDTO extends PageQueryResultDTO<DatabaseDTO> {
    readonly total: number;
    readonly pagination: Nullable<OffsetPaginationDTO>;
    readonly items: KtList<DatabaseDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbDatabaseListQueryResultDTO": unique symbol;
    } & PageQueryResultDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare class CouchdbDatabaseListQuery implements CouchdbDatabaseListQueryDTO {
    constructor(pagination?: Nullable<OffsetPaginationDTO>, channelId?: Nullable<string>, chaincodeId?: Nullable<string>);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get channelId(): Nullable<string>;
    get chaincodeId(): Nullable<string>;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseListQuery;
    }
}
export declare class CouchdbDatabaseListQueryResult implements CouchdbDatabaseListQueryResultDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO>, items: KtList<DatabaseDTO>, total: number);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get items(): KtList<DatabaseDTO>;
    get total(): number;
    readonly __doNotUseOrImplementIt: CouchdbDatabaseListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbDatabaseListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbDatabaseListQueryResult;
    }
}
export declare interface CouchdbSsmGetQueryDTO extends Query {
    readonly channelId: string;
    readonly chaincodeId: string;
    readonly ssmName: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmGetQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbSsmGetQueryResultDTO extends Event {
    readonly uri: SsmUri;
    readonly item: Nullable<SsmDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmGetQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbSsmGetQuery implements CouchdbSsmGetQueryDTO {
    constructor(channelId: string, chaincodeId: string, ssmName: string);
    get channelId(): string;
    get chaincodeId(): string;
    get ssmName(): string;
    readonly __doNotUseOrImplementIt: CouchdbSsmGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmGetQuery;
    }
}
export declare class CouchdbSsmGetQueryResult implements CouchdbSsmGetQueryResultDTO {
    constructor(uri: SsmUri, item: Nullable<Ssm>);
    get uri(): SsmUri;
    get item(): Nullable<Ssm>;
    readonly __doNotUseOrImplementIt: CouchdbSsmGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmGetQueryResult;
    }
}
export declare interface CouchdbSsmListQueryDTO extends PageQueryDTO {
    readonly channelId: string;
    readonly chaincodeId: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmListQueryDTO": unique symbol;
    } & PageQueryDTO["__doNotUseOrImplementIt"];
}
export declare interface CouchdbSsmListQueryResultDTO extends PageQueryResultDTO<SsmDTO> {
    readonly pagination: Nullable<OffsetPaginationDTO>;
    readonly items: KtList<SsmDTO>;
    readonly total: number;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmListQueryResultDTO": unique symbol;
    } & PageQueryResultDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare class CouchdbSsmListQuery implements CouchdbSsmListQueryDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO>, channelId: string, chaincodeId: string);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get channelId(): string;
    get chaincodeId(): string;
    readonly __doNotUseOrImplementIt: CouchdbSsmListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmListQuery;
    }
}
export declare class CouchdbSsmListQueryResult implements CouchdbSsmListQueryResultDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO>, items: KtList<Ssm>, total: number);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get items(): KtList<Ssm>;
    get total(): number;
    readonly __doNotUseOrImplementIt: CouchdbSsmListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmListQueryResult;
    }
}
export declare interface CouchdbSsmSessionStateGetQueryDTO extends Query {
    readonly chaincodeUri: ChaincodeUriDTO;
    readonly ssmName: Nullable<string>;
    readonly sessionName: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbSsmSessionStateGetQueryResultDTO extends Event {
    readonly item: SsmSessionStateDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmSessionStateGetQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbSsmSessionStateGetQuery implements CouchdbSsmSessionStateGetQueryDTO {
    constructor(chaincodeUri: ChaincodeUri, ssmName: Nullable<string>, sessionName: string);
    get chaincodeUri(): ChaincodeUri;
    get ssmName(): Nullable<string>;
    get sessionName(): string;
    readonly __doNotUseOrImplementIt: CouchdbSsmSessionStateGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmSessionStateGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmSessionStateGetQuery;
    }
}
export declare class CouchdbSsmSessionStateGetQueryResult implements CouchdbSsmSessionStateGetQueryResultDTO {
    constructor(item: SsmSessionStateDTO);
    get item(): SsmSessionStateDTO;
    readonly __doNotUseOrImplementIt: CouchdbSsmSessionStateGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmSessionStateGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmSessionStateGetQueryResult;
    }
}
export declare interface CouchdbSsmSessionStateListQueryDTO extends PageQueryDTO {
    readonly chaincodeUri: ChaincodeUriDTO;
    readonly ssm: Nullable<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryDTO": unique symbol;
    } & PageQueryDTO["__doNotUseOrImplementIt"];
}
export declare interface CouchdbSsmSessionStateListQueryResultDTO extends PageQueryResultDTO<SsmSessionStateDTO> {
    readonly pagination: Nullable<OffsetPaginationDTO>;
    readonly items: KtList<SsmSessionStateDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbSsmSessionStateListQueryResultDTO": unique symbol;
    } & PageQueryResultDTO<unknown>["__doNotUseOrImplementIt"];
}
export declare class CouchdbSsmSessionStateListQuery implements CouchdbSsmSessionStateListQueryDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO> | undefined, chaincodeUri: ChaincodeUri, ssm?: Nullable<string>);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get chaincodeUri(): ChaincodeUri;
    get ssm(): Nullable<string>;
    readonly __doNotUseOrImplementIt: CouchdbSsmSessionStateListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmSessionStateListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmSessionStateListQuery;
    }
}
export declare class CouchdbSsmSessionStateListQueryResult implements CouchdbSsmSessionStateListQueryResultDTO {
    constructor(pagination: Nullable<OffsetPaginationDTO>, total: number, items: KtList<SsmSessionStateDTO>);
    get pagination(): Nullable<OffsetPaginationDTO>;
    get total(): number;
    get items(): KtList<SsmSessionStateDTO>;
    readonly __doNotUseOrImplementIt: CouchdbSsmSessionStateListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbSsmSessionStateListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbSsmSessionStateListQueryResult;
    }
}
export declare interface CouchdbUserListQueryDTO extends Query {
    readonly chaincodeUri: ChaincodeUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbUserListQueryDTO": unique symbol;
    } & Query["__doNotUseOrImplementIt"];
}
export declare interface CouchdbUserListQueryResultDTO extends Event {
    readonly items: KtList<AgentDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.couchdb.dsl.query.CouchdbUserListQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class CouchdbUserListQuery implements CouchdbUserListQueryDTO {
    constructor(chaincodeUri: ChaincodeUri);
    get chaincodeUri(): ChaincodeUri;
    readonly __doNotUseOrImplementIt: CouchdbUserListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbUserListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbUserListQuery;
    }
}
export declare class CouchdbUserListQueryResult implements CouchdbUserListQueryResultDTO {
    constructor(items: KtList<Agent>);
    get items(): KtList<Agent>;
    readonly __doNotUseOrImplementIt: CouchdbUserListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace CouchdbUserListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => CouchdbUserListQueryResult;
    }
}
export declare interface DataChaincodeListQueryDTO {
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataChaincodeListQueryDTO": unique symbol;
    };
}
export declare class DataChaincodeListQuery implements DataChaincodeListQueryDTO {
    constructor();
    readonly __doNotUseOrImplementIt: DataChaincodeListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataChaincodeListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataChaincodeListQuery;
    }
}
export declare interface DataChaincodeListQueryResultDTO {
    readonly items: KtList<ChaincodeUri>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataChaincodeListQueryResultDTO": unique symbol;
    };
}
export declare class DataChaincodeListQueryResult implements DataChaincodeListQueryResultDTO {
    constructor(items: KtList<ChaincodeUri>);
    get items(): KtList<ChaincodeUri>;
    readonly __doNotUseOrImplementIt: DataChaincodeListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataChaincodeListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataChaincodeListQueryResult;
    }
}
export declare interface DataQueryDTO {
    readonly ssmUri: SsmUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataQueryDTO": unique symbol;
    };
}
export declare interface DataSsmGetQueryDTO extends DataQueryDTO {
    readonly ssmUri: SsmUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmGetQueryDTO": unique symbol;
    } & DataQueryDTO["__doNotUseOrImplementIt"];
}
export declare class DataSsmGetQuery implements DataSsmGetQueryDTO {
    constructor(ssmUri: SsmUri);
    get ssmUri(): SsmUri;
    readonly __doNotUseOrImplementIt: DataSsmGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmGetQuery;
    }
}
export declare interface DataSsmGetQueryResultDTO {
    readonly item: Nullable<DataSsm>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmGetQueryResultDTO": unique symbol;
    };
}
export declare class DataSsmGetQueryResult implements DataSsmGetQueryResultDTO {
    constructor(item: Nullable<DataSsm>);
    get item(): Nullable<DataSsm>;
    readonly __doNotUseOrImplementIt: DataSsmGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmGetQueryResult;
    }
}
export declare interface DataSsmListQueryDTO {
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmListQueryDTO": unique symbol;
    };
}
export declare class DataSsmListQuery implements DataSsmListQueryDTO {
    constructor(chaincodes: KtList<ChaincodeUri>);
    get chaincodes(): KtList<ChaincodeUri>;
    readonly __doNotUseOrImplementIt: DataSsmListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmListQuery;
    }
}
export declare interface DataSsmListQueryResultDTO {
    readonly items: KtList<DataSsmDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmListQueryResultDTO": unique symbol;
    };
}
export declare class DataSsmListQueryResult implements DataSsmListQueryResultDTO {
    constructor(items: KtList<DataSsm>);
    get items(): KtList<DataSsm>;
    readonly __doNotUseOrImplementIt: DataSsmListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmListQueryResult;
    }
}
export declare interface DataSsmSessionGetQueryDTO extends DataQueryDTO {
    readonly sessionName: string;
    readonly ssmUri: SsmUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionGetQueryDTO": unique symbol;
    } & DataQueryDTO["__doNotUseOrImplementIt"];
}
export declare class DataSsmSessionGetQuery implements DataSsmSessionGetQueryDTO {
    constructor(sessionName: string, ssmUri: SsmUri);
    get sessionName(): string;
    get ssmUri(): SsmUri;
    readonly __doNotUseOrImplementIt: DataSsmSessionGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionGetQuery;
    }
}
export declare interface DataSsmSessionGetQueryResultDTO extends Event {
    readonly item: Nullable<DataSsmSessionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionGetQueryResultDTO": unique symbol;
    } & Event["__doNotUseOrImplementIt"];
}
export declare class DataSsmSessionGetQueryResult implements DataSsmSessionGetQueryResultDTO {
    constructor(item: Nullable<DataSsmSession>);
    get item(): Nullable<DataSsmSession>;
    readonly __doNotUseOrImplementIt: DataSsmSessionGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionGetQueryResult;
    }
}
export declare interface DataSsmSessionListQueryDTO extends DataQueryDTO {
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionListQueryDTO": unique symbol;
    } & DataQueryDTO["__doNotUseOrImplementIt"];
}
export declare class DataSsmSessionListQuery implements DataSsmSessionListQueryDTO {
    constructor(ssmUri: SsmUri);
    get ssmUri(): SsmUri;
    readonly __doNotUseOrImplementIt: DataSsmSessionListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionListQuery;
    }
}
export declare interface DataSsmSessionListQueryResultDTO {
    readonly items: KtList<DataSsmSessionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionListQueryResultDTO": unique symbol;
    };
}
export declare class DataSsmSessionListQueryResult implements DataSsmSessionListQueryResultDTO {
    constructor(items: KtList<DataSsmSession>);
    get items(): KtList<DataSsmSession>;
    readonly __doNotUseOrImplementIt: DataSsmSessionListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionListQueryResult;
    }
}
export declare interface DataSsmSessionLogGetQueryDTO extends DataQueryDTO {
    readonly sessionName: string;
    readonly txId: string;
    readonly ssmUri: SsmUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionLogGetQueryDTO": unique symbol;
    } & DataQueryDTO["__doNotUseOrImplementIt"];
}
export declare class DataSsmSessionLogGetQuery implements DataSsmSessionLogGetQueryDTO {
    constructor(sessionName: string, txId: string, ssmUri: SsmUri);
    get sessionName(): string;
    get txId(): string;
    get ssmUri(): SsmUri;
    readonly __doNotUseOrImplementIt: DataSsmSessionLogGetQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionLogGetQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionLogGetQuery;
    }
}
export declare interface DataSsmSessionLogGetQueryResultDTO {
    readonly item: Nullable<DataSsmSessionStateDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionLogGetQueryResultDTO": unique symbol;
    };
}
export declare class DataSsmSessionLogGetQueryResult implements DataSsmSessionLogGetQueryResultDTO {
    constructor(item: Nullable<DataSsmSessionState>);
    get item(): Nullable<DataSsmSessionState>;
    readonly __doNotUseOrImplementIt: DataSsmSessionLogGetQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionLogGetQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionLogGetQueryResult;
    }
}
export declare interface DataSsmSessionLogListQueryDTO extends DataQueryDTO {
    readonly sessionName: string;
    readonly ssmUri: SsmUriDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionLogListQueryDTO": unique symbol;
    } & DataQueryDTO["__doNotUseOrImplementIt"];
}
export declare class DataSsmSessionLogListQuery implements DataSsmSessionLogListQueryDTO {
    constructor(sessionName: string, ssmUri: SsmUri);
    get sessionName(): string;
    get ssmUri(): SsmUri;
    readonly __doNotUseOrImplementIt: DataSsmSessionLogListQueryDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionLogListQuery {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionLogListQuery;
    }
}
export declare interface DataSsmSessionLogListQueryResultDTO {
    readonly items: KtList<DataSsmSessionState>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.features.query.DataSsmSessionLogListQueryResultDTO": unique symbol;
    };
}
export declare class SsmSessionLogListQueryResult implements DataSsmSessionLogListQueryResultDTO {
    constructor(items: KtList<DataSsmSessionState>);
    get items(): KtList<DataSsmSessionState>;
    readonly __doNotUseOrImplementIt: DataSsmSessionLogListQueryResultDTO["__doNotUseOrImplementIt"];
}
export declare namespace SsmSessionLogListQueryResult {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => SsmSessionLogListQueryResult;
    }
}
export declare interface DataChannelDTO {
    readonly id: string;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.model.DataChannelDTO": unique symbol;
    };
}
export declare class DataChannel implements DataChannelDTO {
    constructor(id: string);
    get id(): string;
    readonly __doNotUseOrImplementIt: DataChannelDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataChannel {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataChannel;
    }
}
export declare interface DataSsmDTO {
    readonly uri: SsmUriDTO;
    readonly ssm: SsmDTO;
    readonly channel: DataChannelDTO;
    readonly version: Nullable<string>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.model.DataSsmDTO": unique symbol;
    };
}
export declare class DataSsm implements DataSsmDTO {
    constructor(uri: SsmUri, ssm: Ssm, channel: DataChannel, version: Nullable<string>);
    get uri(): SsmUri;
    get ssm(): Ssm;
    get channel(): DataChannel;
    get version(): Nullable<string>;
    readonly __doNotUseOrImplementIt: DataSsmDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsm {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsm;
    }
}
export declare interface DataSsmSessionDTO {
    readonly ssmUri: SsmUri;
    readonly sessionName: string;
    readonly state: DataSsmSessionStateDTO;
    readonly channel: DataChannelDTO;
    readonly transaction: Nullable<TransactionDTO>;
    readonly transactions: KtList<TransactionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.model.DataSsmSessionDTO": unique symbol;
    };
}
export declare class DataSsmSession implements DataSsmSessionDTO {
    constructor(sessionName: string, state: DataSsmSessionState, channel: DataChannel, transaction: Nullable<Transaction>, ssmUri: SsmUri, transactions: KtList<TransactionDTO>);
    get sessionName(): string;
    get state(): DataSsmSessionState;
    get channel(): DataChannel;
    get transaction(): Nullable<Transaction>;
    get ssmUri(): SsmUri;
    get transactions(): KtList<TransactionDTO>;
    readonly __doNotUseOrImplementIt: DataSsmSessionDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSession {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSession;
    }
}
export declare interface DataSsmSessionStateDTO {
    readonly details: SsmSessionStateDTO;
    readonly transaction: Nullable<TransactionDTO>;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.model.DataSsmSessionStateDTO": unique symbol;
    };
}
export declare class DataSsmSessionState implements DataSsmSessionStateDTO {
    constructor(details: SsmSessionState, transaction: Nullable<Transaction>);
    get details(): SsmSessionState;
    get transaction(): Nullable<Transaction>;
    readonly __doNotUseOrImplementIt: DataSsmSessionStateDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmSessionState {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmSessionState;
    }
}
export declare interface DataSsmUserDTO {
    readonly agent: AgentDTO;
    readonly __doNotUseOrImplementIt: {
        readonly "ssm.data.dsl.model.DataSsmUserDTO": unique symbol;
    };
}
export declare class DataSsmUser implements DataSsmUserDTO {
    constructor(agent: Agent);
    get agent(): Agent;
    readonly __doNotUseOrImplementIt: DataSsmUserDTO["__doNotUseOrImplementIt"];
}
export declare namespace DataSsmUser {
    /** @deprecated $metadata$ is used for internal purposes, please don't use it in your code, because it can be removed at any moment */
    namespace $metadata$ {
        const constructor: abstract new () => DataSsmUser;
    }
}