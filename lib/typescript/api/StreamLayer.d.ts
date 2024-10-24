export interface StreamLayerDemoEvent {
    id: string;
    title?: string;
    subtitle?: string;
    previewUrl?: string;
    videoUrl?: string;
}
export declare enum StreamLayerInviteGroupType {
    WatchParty = "WatchParty",
    Chat = "Chat"
}
export interface StreamLayerInviteUser {
    id?: string;
    tinodeUserId?: string;
    name?: string;
    username?: string;
    avatar?: string;
}
export interface StreamLayerInvite {
    linkId?: string;
    eventId?: string;
    externalEventId?: string;
    groupId?: string;
    externalGroupId?: string;
    gamification?: boolean;
    groupType?: StreamLayerInviteGroupType;
    user?: StreamLayerInviteUser;
}
export declare class StreamLayer {
    static isInitialized(): Promise<boolean>;
    static authorizationBypass(schema: string, token: string): Promise<void>;
    static useAnonymousAuth(): Promise<void>;
    static isUserAuthorized(): Promise<boolean>;
    static logout(): Promise<void>;
    static createEventSession(id: string): Promise<void>;
    static releaseEventSession(): void;
    static getInvite(json: Object): Promise<StreamLayerInvite>;
    static getDemoEvents(date: string): Promise<Array<StreamLayerDemoEvent>>;
    static initSdk(sdkKey: string, isDebug: boolean): void;
}
//# sourceMappingURL=StreamLayer.d.ts.map