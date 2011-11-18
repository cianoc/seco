
(
s.waitForBoot({
"/home/ggz/code/sc/seco/main.sc".loadDocument;

~synthlib = [
	\sinadsr,
	\vowel,
	\vowel2,
	\kickTrig1,
	\snTrig1,
	\boom1,
	\KSpluck,
	\KSpluck2,
	\KSpluck3,
	\hihat,
	\formant,
	\formant2,
	\formantadsr,
	\pmosc,
	\snare1,
	\perc1,
	\monosampler,
	\stereosampler,
	\ss_comb,
	\ss_combfreq,
	\fmbump,
	\loop1,
	\zyn1,
	\zyn2,
].collect({ arg i; i -> i });

~samplelib = [
	"sounds/perc1.wav",
	"sounds/pok1.wav",
	"sounds/amen-break.wav",
	"sounds/default.wav"
];

~seq = ~mk_sequencer.value;
~seq.load_patlib( ~synthlib );
~seq.load_samplelib_from_path("sounds" );
~seq.make_gui;

});
)
(
s.waitForBoot({

"/home/ggz/code/sc/seco/main.sc".loadDocument;
~seq = ~mk_sequencer.value;
~seq.test_player(\zyn1);

});

)

~seq.save_presets("plop");
~seq.load_presets("plop");

b = Buffer.read(s, "sounds/pok1.wav");
Synth(\ss_comb, [\bufnum, b.bufnum,])

(
var myPath;
myPath = PathName.new("sounds");
myPath.files.collect(_.fullPath).postln;
)


(
// reviewed

SynthDef("perc1", { 
	arg out=0, gate=1, lpf=50, bpf=100, bprq=0.1,
		ratio=4, range=300, carrier=400, amp=0.1, pan=0;
	var mid, ou, env;

	mid = SinOsc.ar(SinOsc.ar(carrier*ratio)*range+carrier);
	mid = BPF.ar(mid, bpf, bprq);
 
 	ou = Klang.ar( `[[50,86,93,136,182],nil,nil] );
	ou = LPF.ar(ou, lpf);
	ou = mid + ou;

	env = EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	ou = ou * env;

	Out.ar(out,Pan2.ar(ou,pan,amp))
 
}).store;

SynthDef("hihat", { arg out=0, amp=0.1, pan=0, gate=1, freq=440, noise=0.5, rq=1.9;
	var ou;
	ou = WhiteNoise.ar(noise);
	ou = ou * EnvGen.kr(~make_adsr.(\adsr),gate,doneAction:2);
	ou = Resonz.ar(ou, freq, rq);
	ou = Pan2.ar(ou,pan,amp);
 
	Out.ar(out, ou);
}).store;

SynthDef("kickTrig1", { arg pan=0, levK=1, t_trig=0, gate=1, f1=36.7, f2=73.4, amp=0.1, out=0;
	var kEnv, ou;
	var kickEnv;
	kickEnv = ~make_adsr.(\adsr);
	kEnv=EnvGen.ar(kickEnv,gate, doneAction:2, levelScale: levK);
	ou = Decay2.kr(t_trig, 0.005, 0.45, FSinOsc.ar(f1, 0.4)+FSinOsc.ar(f2, 0.2));
	ou = Pan2.ar(ou,pan,kEnv * amp);
 
	Out.ar(out, ou);
}).store;

SynthDef("snTrig1", { arg out=0, pan=0, amp=0.1, gate=1, levSn=1, t_trig=0, f1=73.4, noise=0.4;
	var snEnv, ou;
	var snareEnv;
	snareEnv = ~make_adsr.(\adsr);
	snEnv=EnvGen.ar(snareEnv,gate, doneAction:2, levelScale: levSn);
	ou = Decay2.kr(t_trig, 0.005, 0.25, FSinOsc.ar(f1, 0.3) + BrownNoise.ar(noise));
	ou = Pan2.ar(ou,pan,snEnv * amp);

	Out.ar(out, ou);
}).store;

SynthDef("boom1", { 
	arg out=0, pan=0, t_trig=1, attack=0.005, release=1.25, noise=1, f_base=70, 
		mod=4, range=300, carrier=400, amp=0.1;
 
 	var ou;
	ou = Decay2.kr(t_trig, attack, release, BrownNoise.ar(noise) 
		+ SinOsc.ar(f_base)
		+ SinOsc.ar(SinOsc.kr(mod,1.0)*range+carrier)
	);
	DetectSilence.ar(in: ou, amp: 0.001, time: 0.1, doneAction: 2);
	ou = Pan2.ar(ou,pan,amp);

	Out.ar(out,ou)
 
}).store;


SynthDef("snare1", { 
	arg out=0, gate=1, pan=0, amp=0.1, freq=111;
	var mid, ou, ou2, env1, env2;

	ou = LFTri.ar(freq);
	ou = FreqShift.ar(ou, [175, 224]).sum;
	env1 = EnvGen.ar(~make_adsr.(\adsr),gate, doneAction:2);

	env2 = EnvGen.ar(~make_adsr.(\adsr_short),gate);
	ou2 = SinOsc.ar([330,180]).sum*env2;

	ou = ou + ou2;
	ou = ou * env1;
	ou = Pan2.ar(ou,pan,amp);


	Out.ar(out,ou.dup)
 
}).store;

// end reviewed

SynthDef(\zyn1, { arg out=0, freq=200, amp=0.1, pan=0, gate=1,
					freq_am, scale_am,
					freq_fm, scale_fm, scale_freq;
	var ou, fm, am;
	am = SinOsc.ar(~make_rgenadsr.(\adsr_am, gate, freq_am, scale_am));
	fm = SinOsc.ar(~make_rgenadsr.(\adsr_fm, gate, freq_fm, scale_fm)) * ~make_rgenadsr.(\adsr_freq, gate, freq, scale_freq);
	//fm = SinOsc.ar(freq_fm) * ~make_rgenadsr.(\adsr_freq, gate, freq, scale_freq);
	//fm = SinOsc.ar(~make_rgenadsr.(\adsr_fm, gate, freq_fm, scale_fm))*(scale_freq*freq)+freq;
	//fm = ~make_rgenadsr.(\adsr_freq, gate, freq, scale_freq);
	ou = SinOsc.ar(fm) * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2) * am;
	//ou = SinOsc.ar(fm) * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2);
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;

SynthDef(\zyn2, { arg out=0, freq=200, amp=0.1, pan=0, gate=1,
					freq_am, scale_am,
					freq_fm, scale_fm, scale_freq,
					freq_fc, scale_fc, rq;
	var ou, fm, am;
	am = SinOsc.ar(~make_rgenadsr.(\adsr_am, gate, freq_am, scale_am));
	fm = SinOsc.ar(~make_rgenadsr.(\adsr_fm, gate, freq_fm, scale_fm)) * ~make_rgenadsr.(\adsr_freq, gate, freq, scale_freq);
	ou = SinOsc.ar(fm) * EnvGen.ar(~make_adsr.(\adsr),gate,doneAction:2) * am;
	ou = RLPF.ar(ou,~make_rgenadsr.(\adsr_fc, gate, freq_fc, scale_fc), rq); 
	ou = Pan2.ar(ou, pan, amp);
	Out.ar(out, ou);
}).add;

SynthDef(\fmbump, { 
       arg out=0, freq = 220, amp=0.1, pan=0, gate=1, mod_ratio = 0.5, car_ratio = 1, modbias=0, modscale=100, freqscale=10;
       var amp_env, freq_env, mod_env, mod_osc, car_osc, ou;
       amp_env = EnvGen.ar(~make_adsr.(\adsr), gate, doneAction: 2);
       freq_env = EnvGen.ar(~make_adsr.(\adsr_freq), gate, freqscale, freq, doneAction: 2);
       mod_env = EnvGen.ar(~make_adsr.(\adsr_mod), gate, modscale, modbias, doneAction: 2);
       mod_osc = SinOsc.ar(freq * mod_ratio) * mod_env;
       car_osc = SinOsc.ar(freq * car_ratio, mod_osc);
	   ou = car_osc;
       ou = ou * amp_env;
	   ou = Pan2.ar(ou, pan, amp);
       Out.ar(out, ou);
}).add;

SynthDef(\pmosc, {
	arg out=0, amp=0.1, freq=200, freqmod=10, ffreqcar=200, ffreqmod=100, rq=0.1, gate=1;

		var ou, env, envcar, envmod, envidx, envffreq, envrq;

		envcar = freq;
		envmod = EnvGen.kr(~make_adsr.(\adsr_mod),gate) * freqmod;
		envidx = EnvGen.kr(~make_adsr.(\adsr_idx),gate) * 2pi;
		envffreq = EnvGen.kr(~make_adsr.(\adsr_ffreq),gate) * ffreqmod + ffreqcar;
		envrq = EnvGen.kr(~make_adsr.(\adsr_rq),gate) * rq;
		env = EnvGen.kr(~make_adsr.(\adsr),gate,doneAction:2);
		ou = PMOsc.ar(envcar, envmod, envidx);

		ou = RLPF.ar(ou, envffreq, envrq);

		ou = ou*env*amp;


        Out.ar(out, ou)
}).add;

SynthDef(\monosampler, {| out = 0, amp=0.1, pan=0, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0|

	var player,env;
	env = EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2) * amp;
	player = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = Pan2.ar(player, pan, amp);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 1)
))).store;

SynthDef(\stereosampler, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0|

	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\ss_comb, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0, maxdelay=1, delay=0.1, decay=0.1|

	var player,env;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = CombN.ar(player, maxdelay, delay, decay);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\ss_combfreq, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, loop=0, maxdelay=1, freq=200, decay=0.1|

	var player,env;
	var delay = freq.reciprocal;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2);
	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, 1, startPos: (pos*BufFrames.kr(bufnum)), doneAction:2, loop: loop);
	player = CombN.ar(player, maxdelay, delay, decay);
	player = player * env * amp;
	Out.ar(out, player);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\loop1, {| out = 0, amp=0.1, bufnum = 0, gate = 1, pos = 0, speed = 1, freq = 0, endfreq = 0.001, sustain=0.5, wobble = 3, boost = 1|

	var player,env;
	freq = XLine.ar(freq,endfreq,sustain/4);
	freq = freq.cpsmidi + (LFNoise2.ar(3).range(-1,1) * (1/12));
	freq = freq.midicps;
	env =  EnvGen.kr(~make_adsr.(\adsr), gate, doneAction:2) * amp;
	//env = Linen.kr(gate, 0.1,1,0.1,doneAction:2);

	player = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * speed, Impulse.ar(freq), startPos: (pos*BufFrames.kr(bufnum)) + Rand(0,20), doneAction:2, loop: 1) * boost;
	player = RLPF.ar(player, SinOsc.ar(wobble/sustain).range(20000,80), XLine.ar(0.2,0.9,sustain)) * boost;
	//player = SinOsc.ar(200);
	Out.ar(out, player * env);

}, metadata:(specs:(
	bufnum: (numchan: 2)
))).store;

SynthDef(\formant, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bw=10;

	var ou;
	ou = Formant.ar(freq, formfreq, bw);
	ou = ou * Linen.kr(gate, doneAction:2);
	Out.ar(out, ou*amp);


}, metadata:(specs:(
	formfreq: \widefreq.asSpec,
	bw: \widefreq.asSpec
))).add;

SynthDef(\formant2, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bandw=10, rq=0.1;

	var ou, ou2;
	ou = WhiteNoise.ar(1);
	ou = Mix.fill(4, { arg num;
		var stack=2;
		var lou = ou;
		num = num+1;
		stack.do ({ arg i;
			lou = BPF.ar(lou, freq*num, rq);
		});
		lou;
	});
	ou = ou * Linen.kr(gate, doneAction:2) * 10;
	Out.ar(out, ou*amp);
}, metadata:(specs:(
	formfreq: \widefreq.asSpec,
	bandw: \widefreq.asSpec
))).add;

SynthDef(\formantadsr, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bandw=10, rq=0.1;

	var ou, ou2;
	ou = WhiteNoise.ar(1);
	ou = Mix.fill(4, { arg num;
		var stack=2;
		var lou = ou;
		num = num+1;
		stack.do ({ arg i;
			lou = BPF.ar(lou, freq*num, rq);
		});
		lou;
	});
	ou = ou * EnvGen.ar(~make_adsr.(\adsr), doneAction:2);
	Out.ar(out, ou*amp);
}, metadata:(specs:(
	formfreq: \widefreq.asSpec,
	bandw: \widefreq.asSpec
))).add;

SynthDef(\saw, { arg out=0, amp=0.1, gate=1, freq=200, formfreq=300, bw=10;

	var ou;
	ou = Saw.ar(freq);
	ou = ou * Linen.kr(gate, doneAction:2);
	Out.ar(out, ou*amp);


}).add;


SynthDef(\vowel, { arg out=0, amp=0.1, gate=1, freq=200, rqfreq=100, rqoffset=10;
	var env, env2, envf, enva, ou, ou2;
	env = ~make_adsr.(\adsr);
	env2 = ~make_adsr.(\adsr_vowel);
	envf = EnvGen.kr(env2, gate);
	enva = EnvGen.kr(env, gate,doneAction:2);
	ou = Formants.ar(freq,  Vowel(\i, \bass).blend(Vowel(\o, \bass), envf + LFNoise0.kr([5,5.1],mul:0.1,add:1).lag(0.1))) ;
	ou = PinkNoise.ar(1)*0.5 + ou;
	ou2 = BPF.ar(ou, freq+LFNoise0.kr([1.12,5.2],1,20*enva).lag(0.1), SinOsc.kr(rqfreq*enva+rqoffset,0,1,4.1))*5;
	ou = Klank.ar(`[[1.1],[0.1],[0.5]],ou,700) + ou2;
	ou = ou*enva*amp;
	Out.ar(out, ou);

}).store;

SynthDef(\vowel2, { arg out=0, amp=0.1, gate=1, freq=200, rqfreq=100, rqoffset=10, loclip=(-1), hiclip=1;
	var env, env2, envf, enva, ou, ou2;
	env = ~make_adsr.(\adsr);
	env2 = ~make_adsr.(\adsr_vowel);
	envf = EnvGen.kr(env2, gate);
	enva = EnvGen.kr(env, gate,doneAction:2);
	ou = Formants.ar(freq,  Vowel(\i, \bass).blend(Vowel(\o, \bass), envf + LFNoise0.kr([5,5.1],mul:0.1,add:1).lag(0.1))) ;
	ou = PinkNoise.ar(1)*0.5 + ou;
	ou2 = BPF.ar(ou, freq+LFNoise0.kr([1.12,5.2],1,20*enva).lag(0.1), SinOsc.kr(rqfreq*enva+rqoffset,0,1,4.1))*5;
	ou = Klank.ar(`[[1.1],[0.1],[0.5]],ou,700) + ou2;
	ou = Clip.ar(ou*enva, loclip, hiclip);
	Out.ar(out, ou*amp);

}, metadata:(specs:(
	loclip: \bipolar,
	hiclip: \bipolar
))).store;

SynthDef(\sinadsr, {
    arg out=0, amp=0.1, gate=1, freq=440;
    var ou;
    var env, envctl;

    env = Env.adsr(0.02, 0.2, 0.25, 0.1, 1, -4);
    //env = Env.newClear(6);

    envctl = Control.names([\adsr]).kr( env.asArray );
    ou = SinOsc.ar( freq);
    ou = ou * EnvGen.kr(envctl, gate, doneAction:2);
    Out.ar(out, ou * amp)
}).store;


 



SynthDef("KSpluck", { arg freq = 200, delayDecay = 1.0, attack=0.0001, release=0.001, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = PinkNoise.ar(burstEnv);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, delayDecay, add: signalOut);
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar(out, signalOut*amp)
}).store;

SynthDef("KSpluck3", { arg freq = 200, delayDecay = 1.0, attack=0.1, release=0.1, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = PinkNoise.ar(10);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, delayDecay, add: signalOut);
	signalOut = signalOut * burstEnv;
	signalOut = signalOut * amp * 0.1;
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar([0,1], signalOut)
}).store;

SynthDef("KSpluck2", { arg freq = 200, noise=10, bpratio=0.9, rq=0.1, delayDecay = 1.0, attack=0.1, release=0.1, amp=0.1, out=0;
	var burstEnv, att = attack, dec = release;
	var signalOut, delayTime;
	delayTime = [freq, freq * 2].reciprocal;
	burstEnv = EnvGen.kr(Env.perc(att, dec));
	signalOut = WhiteNoise.ar(noise);
	signalOut = BPF.ar(signalOut, freq*bpratio, rq);
	signalOut = CombL.ar(signalOut, delayTime, delayTime, [delayDecay, delayDecay*1.0.rand], add: signalOut);
	signalOut = signalOut * burstEnv;
	signalOut = signalOut * amp * 0.01;
	DetectSilence.ar(signalOut, doneAction:2);
	Out.ar(out, signalOut.dup)
}).store;


)
