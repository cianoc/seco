(
s.waitForBoot{
Window.closeAll;
~seq = Mdef.force_init(true);
//~seq.init_midi;
~synthlib = [
	\seqnode,
	\parnode,
	\audiotrack_expander,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,

	"ci op_matrix2",
	"ci mosc",
	"ci moscfilter",
	"ci moscfilter_modfx",
	"ci osc3filter2",
	"ci bufosc_filt",
	"ci bufosc_filt_spread",
	"ci samplerfilter2",

	//"ci inlinefx",
	"ci inlinegen",
	"ci inline_genfx",

	\bufsin1,
	\zegrainer,
	\sampleosc1,
	\sampleosc2,

	\osc1,
	\osc_lag,
	\osc_test,
	\noisecomb,
	\moteur,
	\guitar,
	\guitar2,
	\ch,
	\membraneHex,

	\kick1,
	\kick2,
	\kick3,
	\kicklank,
	\snare1,
	\snare2,
	\kraftySnr,

	\lead2,
	\saxo,
	\organ,
	\noisescape,
].collect({ arg i; i -> i });
~seq.load_patlib( ~synthlib );


~effectlib = [
	\comb1,
	//\freeverb,
	\p_reverb,
	\p_flanger,
	\p_chorus,
	\p_delay,
	\p_comb,
	"ci insertfx3",
	\dubecho,
	\dubecho_inmix,
	\dubecho_orig,
	\limiter,
	\bufstut,
	\multitap8,
	\ir_reverb,
].collect({arg i; i -> i });
~seq.load_effectlib( ~effectlib );

~modlib = [
	\setbus,
	\modenv,
	\gater,
	\gated_asr,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	\varline1,
	"ci mod_osc",
	"ci mod_envosc",
	"ci dadsr_kr",
	"ci custom_env",
	"ci selfgated_env",
	\cuspl,
].collect({arg i; i -> i });
~seq.load_modlib( ~modlib );

~inlinegenlib = [
	\empty,
	"ci ingen_osc",
	"ci sin",
];
~seq.load_inlinegenlib( ~inlinegenlib );

~inlinefxlib = [
	\empty,
	"ci infx_filter",
];
~seq.load_inlinefxlib( ~inlinefxlib );

~seq.set_presetlib_path("mypresets");

Mdef.side_gui;

~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

Mdef.main.samplekit_manager.parse_samplekit_dir;
Mdef.main.samplekit_manager.parse_samplekit_dir(~seco_root_path +/+ "hydrogenkits/");

}
)


~node = Mdef.node_by_index(0)
~node = Mdef.node_by_index(1)
Mdef.node_by_index(0).is_mono = false;
~node.is_pmonoartic = true;
~node.rebuild_arg_list
~node.get_scoreset.get_notescore.remove_first_rest_if_not_needed = true

(
SynthDef(\osc_lag, { arg out, gate=1, pan=0, freq=300, amp=0.1, ffreq=200, rq=0.1, attack=0.1, release=0.1, doneAction=2, freqlag=0.2;
	var sig, env;
	freq = freq.lag(freqlag);
	sig = LFSaw.ar(freq * [1,1.01,0.99]);
	env = EnvGen.kr(Env.adsr(attack,0.1,1,release), gate, doneAction:doneAction);
	sig = RLPF.ar(sig, [ffreq, freq, ffreq*2, freq*2], rq);
	sig = AllpassC
	sig = sig.sum;
	sig = sig * env;
	sig = Pan2.ar(sig, pan, 1);
	sig = sig * amp;
	Out.ar(out, sig);
}).store;
)

(
SynthDef(\noisecomb, { arg out, gate=1, pan=0, freq=300, amp=0.1, noiselevel=0.2,
		minfreq=100, decaytime=1, doneAction=2, ffreq=500, rq=0.3;
	var sig, env;
	sig = WhiteNoise.ar(noiselevel);
	sig = sig + ClipNoise.ar(noiselevel);
	sig = sig + AllpassC.ar(sig, 1/minfreq, 1/freq, decaytime);
	sig = RLPF.ar(sig, ffreq, rq);
	env = EnvGen.kr(\adsr.kr(Env.adsr(0.6,0.1,0.8,0.1)), gate, doneAction:doneAction);
	sig = sig * env;
	sig = Pan2.ar(sig, pan, 1);
	sig = sig * amp;
	Out.ar(out, sig);
}).store;
)

(
SynthDef(\noisecomb1, { arg out, gate=1, pan=0, freq=300, amp=0.1, attack=0.1, release=0.1, noiselevel=0.2,
		minfreq=100, decaytime=1, doneAction=2;
	var sig, env;
	sig = WhiteNoise.ar(noiselevel);
	sig = sig + AllpassC.ar(sig, 1/minfreq, 1/freq, decaytime);
	env = EnvGen.kr(Env.adsr(attack,0.1,1,release), gate, doneAction:doneAction);
	sig = sig * env;
	sig = Pan2.ar(sig, pan, 1);
	sig = sig * amp;
	Out.ar(out, sig);
}).store;
)

Ndef(\plop, { AllpassC.ar(Decay.ar(Impulse.ar(80), 0.17, BrownNoise.ar), 0.2, 0.4, 0.71) * 0.1 ! 2}).play;

(
SynthDef(\moteur, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, impulse_rate=70, 
		noise_decaytime=0.17, decaytime=0.7, doneAction=2, maxdelaytime=0.2, delaytime=0.2;
	var sig;
	sig = AllpassC.ar(Decay.ar(Impulse.ar(impulse_rate), noise_decaytime, BrownNoise.ar), maxdelaytime, delaytime, decaytime);
	sig = sig * EnvGen.ar(Env.adsr(0.01,0.1,0.8,0.1),gate,doneAction:doneAction);
	sig = Pan2.ar(sig, pan, amp);
	Out.ar(out, sig);
}).store;
)
Ndef(\plop, { AllpassC.ar(Decay.ar(Impulse.ar(80), 0.17, BrownNoise.ar), 0.2, 0.4, 0.71) * 0.1 ! 2}).play;

~reload_node.("noisecomb_l1038")

Env.adsr(0.3,1,2,3).asCompileString
a = Env.adsr(0.3,1,2,3).asArray
a = Env.adsr(0.3,1,2,3).dump
b = Env.xyc(a.clump(3))
a.dump

(

SynthDef(\osc_test, { arg out, gate=1, freq=300, amp=0.1, ffreq=200, rq=0.1, attack=0.1, release=0.1, doneAction=2, pan=0;
	var sig = LFSaw.ar(freq);
	//var env = EnvGen.kr(Env.adsr(attack,0.1,1,release), gate, doneAction:doneAction);
	var env = EnvGen.kr(\adsr.kr(Env.adsr(0.1,0.1,1,0.1)), gate, doneAction:doneAction);
	sig = RLPF.ar(sig, ffreq, rq);
	sig = sig * env;

	sig = Pan2.ar(sig, pan, 1);
	sig = sig * amp;
	Out.ar(out, sig);
}).store;
)


"~/code/sc/seco/synthpool.sc".standardizePath.load

(
	Ndef(\fb, {
		var sig;
		sig = SoundIn.ar([0,1]);
		//sig = sig.clip(0.5);
		sig = HPF.ar(sig, 100);
		sig = LPF.ar(sig, SinOsc.ar(0.4).range(300,800));
		//sig = CombL.ar(sig, 0.5,0.125,8.5);
		sig = sig * SinOsc.ar(3.2).range(0,1);
		sig = sig + DelayL.ar(sig, 1, 1);
		sig = LeakDC.ar(sig);
		sig = sig * 0.9;
		sig = Limiter.ar(sig, 0.01, 0.2);
		sig;
	}).play
)


(
	{ SinOsc.ar(300) }.play
)


(
	
	SynthDef(\cuspl, { arg out=0, amp=1, pa=1, pb=1.5, xi=2, ifreq=200;
		var sig;
		sig = CuspL.ar(ifreq, pa, pb, xi);
		sig = sig * amp;
		Out.kr(out, sig);
	}).store;
)




(
	SynthDef(\noisescape, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, doneAction=2,
			lpf_white=5000, hpf_white=200, amp_white=1,
			lpf_clip=5000, hpf_clip=200, amp_brown=1,
			lpf_brown=5000, hpf_brown=200, amp_clip=1;
		var sigw, sigb, sigc;
		var sig;
		sigw = WhiteNoise.ar(amp_white);
		sigw = LPF.ar(sigw, lpf_white);
		sigw = HPF.ar(sigw, hpf_white);
		sigw = sigw * EnvGen.ar(\adsr_white.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:0);

		sigb = WhiteNoise.ar(amp_brown);
		sigb = LPF.ar(sigb, lpf_brown);
		sigb = HPF.ar(sigb, hpf_brown);
		sigb = sigb * EnvGen.ar(\adsr_brown.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:0);

		sigc = ClipNoise.ar(amp_clip);
		sigc = LPF.ar(sigc, lpf_clip);
		sigc = HPF.ar(sigc, hpf_clip);
		sigc = sigc * EnvGen.ar(\adsr_clip.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:0);

		sig = sigw + sigb + sigc;
		sig = sig * EnvGen.ar(\adsr.kr(Env.adsr(0.01,0.1,0.8,0.1)),gate,doneAction:doneAction);
		sig = Pan2.ar(sig, pan, amp);
		Out.ar(out, sig);
	}).store;
)

(
SynthDef(\ch, { | out=0, decay = 3, amp = 0.1, freqfactor = 1, doneAction=2, delayfactor=1, shift=(-200) |
	var sig = WhiteNoise.ar;
	var del;
	sig = LPF.ar(sig, (12000*freqfactor).clip(10,15000));
	//sig = sig + DelayC.ar(sig, 0.1,LFNoise2.ar(1/2).range(0.001,0.01)*delayfactor);
	del = DelayC.ar(sig, 0.1,LFNoise2.ar(1/2).range(0.001,0.01)*delayfactor);
	del = FreqShift.ar(del, shift);
	sig = sig + del;
	sig = HPF.ar(sig, (4000*freqfactor).clip(10,15000), 0.05);
	sig = sig * EnvGen.kr(Env.perc(0.01,decay*0.8), doneAction:doneAction);
	Out.ar(out, 15 * sig.dup * amp);
}, metadata: (
	specs: (
		shift: ControlSpec.new((-2000),2000, \lin, 0, 0)
	)
)).store;
)



Mdef.main.save_project("plage");
Mdef.main.load_project("plage");



(

SynthDef(\saklangbell, {arg freq=400, amp=0.4, gate=1, pan=0.0; // we add a new argument

	var signal, env;

	//env = EnvGen.ar(Env.adsr(0.01,0.3,0.05,0.7), gate, doneAction:2); // doneAction gets rid of the synth
	env = EnvGen.ar(Env.perc(0.01,0.7), gate, doneAction:2); // doneAction gets rid of the synth

	signal = Klang.ar(`[
		freq*\freqs.kr([0.745,2.122,3.453,4.5454,0.878]),
		\amps.kr([0.25, 0.25, 0.25, 0.25, 0.25]), 
		\rings.kr([0.1,0.1,0.1,0.1,0.1])
	]) * env;

	signal = Pan2.ar(signal, pan);

	Out.ar(0, signal);

}).add;

SynthDef(\klankbell, {arg freq=400, amp=0.4, gate=1, pan=0.0; // we add a new argument

	var signal, env;
	var input;

	
	//env = EnvGen.ar(Env.adsr(0.01,0.3,0.05,0.7), gate, doneAction:2); // doneAction gets rid of the synth
	input = PinkNoise.ar(1);
	env = EnvGen.ar(Env.perc(0.01,0.7), gate, doneAction:2); // doneAction gets rid of the synth

	signal = DynKlank.ar(`[
		freq*\freqs.kr([0.745,2.122,3.453,4.5454,0.878]),
		\amps.kr([0.25, 0.25, 0.25, 0.25, 0.25]), 
		\rings.kr([0.1,0.1,0.1,0.1,0.1])
	], input) * env;

	signal = Pan2.ar(signal, pan, amp);

	Out.ar(0, signal);

}).add;

)

(
	~make_multislider = { arg data, keys;

		var layout;
		n=5;
		w = Window.new.front;
		layout = VLayout.new;
		w.layout = layout;
		keys.do { arg key;
			m = MultiSliderView(w,Rect(10,10,n*13+2,100)); //default thumbWidth is 13
			m.value=Array.fill(n, {|v| v*0.05}); // size is set automatically when you set the value
			m.elasticMode = true;
			m.fillColor = Color.green;
			m.indexThumbSize = 100;
			m.isFilled = true;
			m.action = { arg ms;
				data[key] = ms.value
			};
			layout.add(m)
		};
	}

)

SynthDescLib[\kicklank]
SynthDescLib.global.synthDescs[\kicklank].controlDict[\freqs].dump


Synth(\saklangbell, [\freq, 200])

~freqs = [1,1,1,1,1];
~freqs
(

~spec = ControlSpec(0.01,4,\lin,0,1);
~num = 5;
~data = (
	freqs: 1 ! ~num,
	amps: 1 ! ~num,
	rings: 1 ! ~num,
);
)

~make_multislider.(~data, [\freqs, \amps, \rings])

~data

(
Pdef(\plop, Pbind(
	\instrument, \klankbell,
	\freq, Pseq([300, 201, 243, 215],inf),
	\freqs, Pfunc{ [~spec.map(~data.freqs)] },
	\amps, Pfunc{ [~spec.map(~data.amps)] },
	\rings, Pfunc{ [~spec.map(~data.rings)] },
	\dur, Pwhite(0.12,1,inf),
	\amp, 0.1
)).play;
);

(
	n=5;
	w = Window.new.front;
	m = MultiSliderView(w,Rect(10,10,n*13+2,100)); //default thumbWidth is 13
	m.value=Array.fill(n, {|v| v*0.05}); // size is set automatically when you set the value
	m.elasticMode = true;
	m.fillColor = Color.green;
	m.indexThumbSize = 100;
	m.isFilled = true;
	m.action = { arg q;
	    q.value.postln;
	};

)



(
n=40;

w = Window("MultiSlider Options", Rect(200, Window.screenBounds.height-550, 600, 450));
f={ 
    w.view.decorator = FlowLayout( w.view.bounds, 10@10, 10@2 );
    m = MultiSliderView(w,Rect(0,0,580,200)); // default thumbWidth is 13
    m.value=Array.fill(n, {|v| 0.5+((0.3*v).sin*0.25)});
    m.action = { arg q;q.value.postln; };

    StaticText(w,380@18).string_("indexThumbSize or thumbSize");
    Slider(w,580@10).action_({arg sl; m.indexThumbSize=sl.value*24}).value_(0.5);
    StaticText(w,380@18).string_("valueThumbSize");
    Slider(w,580@10).action_({arg sl; m.valueThumbSize=sl.value*24}).value_(0.5);
    StaticText(w,580@18).string_("xOffset or gap");
    Slider(w,580@10).action_({arg sl; m.xOffset=sl.value*50});
    StaticText(w,580@18).string_("startIndex");
    Slider(w,580@10).action_({arg sl; m.startIndex = sl.value *m.size};);

    CompositeView(w,580@10);//spacer
    Button(w,100@20).states_([["RESET",Color.red]])
        .action_({ w.view.removeAll; f.value; });
    h=StaticText(w,450@18).string_("").stringColor_(Color.yellow);
    Button(w,100@20).states_([["elasticMode = 0"],["elasticMode = 1",Color.white]])
        .action_({|b| m.elasticMode = b.value});
    Button(w,160@20).states_([["indexIsHorizontal = false"],["indexIsHorizontal = true",Color.white]])
        .action_({|b| m.indexIsHorizontal = b.value.booleanValue}).value_(1);
    Button(w,120@20).states_([["isFilled = false"],["isFilled = true",Color.white]])
        .action_({|b| m.isFilled = b.value.booleanValue});
    Button(w,120@20).states_([["drawRects = false"],["drawRects = true",Color.white]])
        .action_({|b| m.drawRects = b.value.booleanValue}).valueAction_(1);
    Button(w,100@20).states_([["drawLines = false"],["drawLines = true",Color.white]])
        .action_({|b| m.drawLines = b.value.booleanValue});
    Button(w,160@20).states_([["readOnly = false"],["readOnly = true",Color.white]])
        .action_({|b| m.readOnly = b.value.booleanValue});
    Button(w,120@20).states_([["showIndex = false"],["showIndex = true",Color.white]])
        .action_({|b| m.showIndex = b.value.booleanValue});
    Button(w,120@20).states_([["reference = nil"],["reference filled",Color.white],["reference random",Color.yellow]])
        .action_({|b| b.value.booleanValue.if({
            (b.value>1).if(
                {m.reference=Array.fill(n, {1.0.rand})},
                {m.reference=Array.fill(m.size, {0.5})});
                },{ q=m.value;m.reference=[]; h.string="reference can't be returned to nil presently. please hit RESET."}
            )
        });
    Button(w,180@20).states_([["fillColor = Color.rand"]]).action_({m.fillColor=Color.rand});
    Button(w,180@20).states_([["strokeColor = Color.rand"]]).action_({m.strokeColor=Color.rand});
    Button(w,180@20).states_([["background = Color.rand"]]).action_({m.background=Color.rand});

};
f.value;
w.front;

)
