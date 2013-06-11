MIDIClient.initialized
// embedInStream dans un Prout
(
s.waitForBoot{
//"/home/ggz/code/sc/abcparser.sc".load;
//"/home/ggz/code/sc/seco/classinstr.sc".load;
Window.closeAll;
~seq = Mdef.force_init(true);
~seq.init_midi;
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

	\bufsin1,
	\zegrainer,
	\sampleosc1,
	\sampleosc2,

	\osc1,
	\guitar,
	\guitar2,
	\ch,

	\kick1,
	\kick2,
	\kick3,
	\kicklank,
].collect({ arg i; i -> i });
~seq.load_patlib( ~synthlib );


~effectlib = [
	\comb1,
	\freeverb,
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
].collect({arg i; i -> i });

~modlib = [
	\setbus,
	\modenv,
	\gater,
	\gated_asr,
	\lfo1,
	\lfo_tri,
	\lfo_asr,
	\line1,
	"ci mod_osc",
	"ci mod_envosc",
	"ci dadsr_kr",
	"ci custom_env",
	"ci selfgated_env",
].collect({arg i; i -> i });

//~samplelib = [
//	"sounds/perc1.wav",
//	"sounds/pok1.wav",
//	"sounds/amen-break.wav",
//	"sounds/default.wav"
//];
~seq.load_effectlib( ~effectlib );
~seq.load_modlib( ~modlib );
~seq.set_presetlib_path("mypresets");
//~seq.append_samplelib_from_path("sounds/" );
//~seq.append_samplelib_from_path("sounds/hydrogen/GMkit" );
//~seq.append_samplelib_from_path("sounds/hydrogen/HardElectro1" );

//Mdef.samplekit(\deskkick, 20.collect{arg i; "/home/ggz/Musique/recording" +/+ i ++ ".wav"});
//Mdef.main.model.bus_mode_enabled = false;

Mdef.side_gui;



~tf = Pfunc({ arg ev; if(ev[\stepline] == 1) { \note } { \rest } });
~ff = Pfunc({ arg ev; if(ev[\stepline1] == 1) { 1 } { \rest } });

//Debug.enableDebug = false;
~windowize = { arg layout;
	var win;
	win = Window.new;
	win.layout = layout;
	win.front;
};

//Mdef.samplekit(\castor, [
//	"~/Musique/beast.wav".standardizePath,
//	"~/Musique/blabla2.wav".standardizePath,
//]);

Mdef.main.samplekit_manager.parse_samplekit_dir;
Mdef.main.samplekit_manager.parse_samplekit_dir(~seco_root_path +/+ "hydrogenkits/");

}
)
~seco_root_path +/+ "hydrogenkits/"
SynthDescLib.global.synthDescs[\gater]
SynthDescLib.global.synthDescs['s_ci selfgated_env_l1036']
SynthDescLib.global.synthDescs[\rah]
"~/code/sc/seco/classinstr.sc".standardizePath.load
"~/code/sc/seco/synthpool.sc".standardizePath.load
0.04*32
1/32

(
SynthDef(\rah, { arg out=0, amp=0.1, gate=1, pan=0, freq=200;
	var ou;
	var trig;
	trig = \bla.tr(1);
	ou = SinOsc.ar(freq);
	ou = ou * EnvGen.ar(Env.linen(0.1,1,0.1),trig,doneAction:0);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)

a  = Synth(\rah)
a.set(\bla, 1)

Platform.resourceDir

Set.newFrom(["bla", "bla"])

["bla", "rah"].includesEqual("bla")
["bla", "rah"].drop(-1)

(
var myPath;
myPath = PathName.new("~/Musique/samplekit/");
"ob".debug;
myPath.folders.do { arg dir;
	var samplekit_name;
	var samples = List.new;
	"iob".debug;
	//dir.postln;
	samplekit_name = dir.folderName;
	samplekit_name.debug("gueule");
	dir.files.do { arg file;
		samples.add( file.fullPath )
	};
	samples.debug("samples");
	Mdef.samplekit(samplekit_name.asSymbol, samples);
	///dir.
};
)

(
~parse_samplekit_dir = { arg samplekit_dir;
	var path;
	samplekit_dir = samplekit_dir ?? "~/Musique/samplekit/";
	path = PathName.new(samplekit_dir);
	path.folders.do { arg dir;
		var samplekit_name;
		var samples = List.new;
		samplekit_name = dir.folderName;
		dir.files.do { arg file;
			samples.add( file.fullPath )
		};
		Mdef.samplekit(samplekit_name.asSymbol, samples);
	};
};
)


(
)

Mdef.main.model.bus_mode_enabled = true
Mdef.main.model.bus_mode_enabled = false
Mdef.main.model.bus_mode_enabled


Mdef.main.save_project("dev1");
Mdef.main.load_project("dev1");

Mdef.main.save_project("books");
Mdef.main.load_project("books");

Mdef.main.save_project("dev1.test1");
Mdef.main.load_project("dev1.test1");

Mdef.main.save_project("testxruns");
Mdef.main.load_project("testxruns");

Mdef.main.save_project("noxruns");
Mdef.main.load_project("noxruns");

Mdef.main.save_project("happypunk");
Mdef.main.load_project("happypunk");

Mdef.main.save_project("acidbass");
Mdef.main.load_project("acidbass");

Mdef.main.save_project("dubpourri");
Mdef.main.load_project("dubpourri");

Mdef.main.save_project("dubpourri2");
Mdef.main.load_project("dubpourri2");

Mdef.main.save_project("noisy");
Mdef.main.load_project("noisy");

Mdef.main.save_project("reggaeton_drum");
Mdef.main.load_project("reggaeton_drum");

Mdef.main.save_project("reggae_pourri");
Mdef.main.load_project("reggae_pourri");

Mdef.main.save_project("truc");
Mdef.main.load_project("truc");

Mdef.main.save_project("bizzare_1");
Mdef.main.load_project("bizzare_1");

Mdef.main.save_project("bizzare_2");
Mdef.main.load_project("bizzare_2");

Mdef.main.save_project("bizzare_3");
Mdef.main.load_project("bizzare_3");

Mdef.main.save_project("rampant");
Mdef.main.load_project("rampant");

Mdef.main.save_project("rampant2");
Mdef.main.load_project("rampant2");

Mdef.main.save_project("cuisine");
Mdef.main.load_project("cuisine");

Mdef.main.save_project("bizzare_3_line");
Mdef.main.load_project("bizzare_3_line");

Mdef.main.save_project("prog1");
Mdef.main.load_project("prog1");

Mdef.main.save_project("testperf");
Mdef.main.load_project("testperf");

Mdef.main.save_project("prog2");
Mdef.main.load_project("prog2");

Mdef.main.save_project("profondeur");
Mdef.main.load_project("profondeur");

Mdef.main.save_project("hurlement");
Mdef.main.load_project("hurlement");

Mdef.main.play_manager

Debug.enableDebug = true;
Debug.enableDebug = false;

"bla".findReplace("niark", "blu")

s.latency = 3
s.latency = 1.2
s.latency = 1.0
s.latency = 0.2
s.latency = 0.4
s.latency = 0.6
s.latency

"/home/ggz/Musique/sc/vipere/ventregris.wav".pathExists

{ SinOsc.ar(100) }.play


Mdef.node("setbus_l1003")

Mdef.sample(\)

Mdef.node("setbus_l1011").get_arg(\scoreline).get_scoreset.get_notescore.notes
Mdef.node("osc1_l1073").get_macro_args
Mdef.node("osc1_l1073")

(
SynthDef(\plop, { arg out=0, amp=0.1, gate=1, pan=0, freq=200, tsustain, t_trig=1;
	var ou;
	ou = SinOsc.ar(freq);
	//tsustain.poll;
	//Trig.kr(t_trig,tsustain);
	ou = ou * EnvGen.ar(Env.linen(0.4,tsustain,0.4),t_trig,doneAction:0);
	ou.poll;
	ou = ou * EnvGen.ar(Env.adsr(0.4,0.1,0.8,0.4),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;
)


(
 Pmono(\plop,
 	\freq, Pseq([100,200],4),
	\dur, 2,
	\legato, 0.1,
	\tsustain, Pkey(\sustain) / Ptempo(),
 ).trace.play

)
TempoClock.default.tempo = 2


Mdef.node_by_index(0).build_real_sourcepat
Mdef.node_by_index(0).wrapper
Pwhite

Mdef.main.commands.commands.keys


(

~resp = Mdef.main.commands.make_binding_responder(\bla, 
	[\test, {
		"TEST".postln;

	} ]

);

~kbresp = ~resp.get_kb_responder(\bla)
~resp
)



a = ~make_seqplayer.(Mdef.main)
a.get_displayable_args
a.update_ordered_args




(
 a = (
 	bla: 3,
	rah: 8,
	arr: [1,2,3],
	cho: \bla,
	gett: { arg self; self[self.cho].debug("result") },

	 );

 b = (
 	parent: a,
	cho: \rah,
 
	 )

)



a.gett
b.gett
b.bla = 2

a.rah = 7

b.arr[0]= 100
a

(
SynthDef(\freeverb, { arg out=0, in, mix=0.5, gate=1, room=0.5, damp=0;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	out.poll;
	Out.ar(out, sig);
}).store;

SynthDef(\freeverb2, { arg out=0, in, mix=0.5, gate=1, room=0.5, damp=0;
	var sig;
	in = In.ar(in, 2);
	sig = FreeVerb.ar(in, mix, room, damp);
	out.poll;
	Out.ar(out, sig);
}).store;
)



(
Pmono(\freeverb2,

	\dur, Pn(1,2)
).play

)




"~/code/sc/seco/synthpool.sc".standardizePath.load
"~/code/sc/seco/classinstr.sc".standardizePath.load






~b1 = Bus.audio(s,2);
~b2 = Bus.audio(s,2);
(
~fxgroup = Group.new(s);
~addaction = 1;
~freeverb = Pmono(\freeverb,
	\in, ~b1,
	\mix, 0.5,
	\addAction, ~addaction,
	\group, ~fxgroup,
	\room, 0.5,
	\dur, Pn(0.25,8),
	\out, ~b2,
);
~freeverb2 = Pmono(\freeverb2,
	\in, ~b2,
	\mix, 0.5,
	\group, ~fxgroup,
	\addAction, ~addaction,
	\room, 0.5,
	\dur, Pn(0.25,8),
	\out, 0,
);

~pat = Pbind(
	\dur, Pn(0.25,8),
	\out, ~b1,
);

//~mainpat = Pspawner({ arg spawner;
//	spawner.par(~pat);
//	[~freeverb, ~freeverb2].do { arg pat;
//		spawner.par(pat)
//	};
//
//});

~mainpat = Ppar([
	~pat,
	~freeverb2,
	~freeverb,
]);

//~pat.play;
//~freeverb.play;
//~freeverb2.play;
Pn(~mainpat,8).play



) 



MIDI

~seq.panels.side.timeline.changed(\blocks)
~seq.panels.side.timeline.timeline_view.timeline.createNode(30,30)
~seq.panels.side.timeline.timeline_view.timeline


(
~tl = ParaTimeline.new;
~tl.userView.background = Color.red;
~win = Window.new;
~win.layout = VLayout.new(~tl.userView);
~win.front;



)
~tl.createNode(30,30);

{ SoundIn.ar([0,1]) }.play


<<<<<<< HEAD
(
Instr(\ci_noise, { arg kind, amp=0.1;
	//TODO
	var sig;
	sig = switch(kind,
		\white, {
			WhiteNoise.ar(amp);
		},
		\pink, {
			PinkNoise.ar(amp);
		},
		\brown, {
			BrownNoise.ar(amp);
		},
		\gray, {
			GrayNoise.ar(amp);
		},
		\clip, {
			ClipNoise.ar(amp);
		},
		{
			//kind.debug("p_noise: ERROR: noise kind not found");
			WhiteNoise.ar(amp);
		}
	);
	sig;

}, [NonControlSpec()]);
)


(
Instr(\blai, {
	
	Instr(\ci_noise).value((kind:\white))

}).asSynthDef(\bli)
)

Patch(\blai).play

Synth(\bli)
=======
"/home/ggz/.vimrc".pathExists.dump
>>>>>>> f8955caae5e52a8b910dfbd3898470b976933e73

PathName("./l/home").isRelativePath
PathName("/home/ggz/Musique").asRelativePath("/hoime").dump
root: /home/tytel/Musique/sc/
in: /home/tytel/Musique/sc/hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac
out: ../hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac
PathName("/home/tytel/Musique/sc/hydrogenkits/YamahaVintageKit/Szl_Cym_02.flac").asRelativePath("/home/tytel/Musique/sc");

o = Server.default.options;
o.memSize 
o.numPrivateAudioBusChannels
o.numAudioBusChannels


Mdef.node("setbus_l1003").get_arg(\scoreline).get_notes
Mdef.node("setbus_l1003").subkind
Mdef.node("setbus_l1007").get_arg(\scoreline).get_notes
Mdef.node("setbus_l1007").get_arg(\scoreline).get_scoreset.sheets.do { arg sh, i; sh.notNil and:{ sh.notes.debug(i+"============")  }}
Mdef.node("setbus_l1007").get_arg(\scoreline).get_scoreset.current_sheet
Mdef.node("setbus_l1003").get_arg(\scoreline).get_scoreset.current_sheet
Mdef.node("setbus_l1003").get_arg(\scoreline).get_scoreset.sheets.do { arg sh, i; sh.notNil and:{ sh.notes.debug(i+"============")  }}

a = Dictionary.new
a.size
a[\a] = 1
a[\b] = 1
a[\b] = nil
a





Mdef.node("ci osc3filter2_l1021").modulation.get_modulation_mixers.keys.do(_.postln)
Mdef.node("ci osc3filter2_l1021").modulation.modulation_mixers[\wtpos_spread] = nil
