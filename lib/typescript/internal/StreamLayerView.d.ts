import React, { PureComponent } from 'react';
import { StreamLayerViewProps, StreamLayerInvite, StreamLayerViewOverlay } from 'react-native-streamlayer';
declare class LBarState {
    slideX: number;
    slideY: number;
    constructor(slideX: number, slideY: number);
}
interface StreamLayerViewState {
    volumeBeforeDucking: number | undefined;
    lbarState: LBarState;
}
export declare class StreamLayerView extends PureComponent<React.PropsWithChildren<StreamLayerViewProps>, StreamLayerViewState> {
    private readonly _root;
    static initialState: StreamLayerViewState;
    constructor(props: StreamLayerViewProps);
    private registerEvents;
    private _onNativeRequestStream;
    private _onNativeLBarStateChanged;
    private _onNativeRequestAudioDucking;
    private _onNativeDisableAudioDucking;
    componentDidMount(): void;
    componentWillUnmount(): void;
    hideMenu(): void;
    hideOverlay(): void;
    showOverlay(viewOverlay: StreamLayerViewOverlay): void;
    handleInvite(invite: StreamLayerInvite): void;
    render(): JSX.Element;
}
export {};
//# sourceMappingURL=StreamLayerView.d.ts.map