
~launchpad_scene_tile = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_play: \green,
	color_off: \off,
	color_buffer: \dimred,
	color_buffer_play: 0x3d,
	range_state: Array.fill(8, 0),
	ranges: [6,7],
	state_edit: { arg self; self.sceneset.state_edit },

	state_play: { arg self; self.sceneset.state_play },
	//state_buffers: { arg self; self.scenestate.state_buffers },
	state_buffers: { arg self; ~veco.get_buffer_presence },

	reset_play_state: { arg self; 
		self.sceneset.state_play = Array2D.new(8,8);
		~veco.counter = IdentityDictionary.new;
		self.update_display;
	},
	
	render_state: { arg self;
		var cleanup;
		~veco.update_buffer_presence;
		self.state_play.rowsDo { arg column, x;
			column.do { arg val, y;
				//[x, y, val].debug("state_leds");
				self.state[x,y] = self.state_to_color(Point(x,y))
			}
		};
		8.do { arg y;
			self.state[8,y] = self.color_off;
		};
	},



	state_to_color: { arg self, pos;
		var x = pos.x;
		var y = pos.y;
		var val = self.state_play[x,y];
		//[val,self.state_buffers[x,y], pos].debug("state_to_color: play, buf, pos");
		if(val == 1) { 
			if(self.state_buffers[x,y] == 1) {
				self.color_buffer_play
			} {
				self.color_play

			}
		} { 
			if(self.state_buffers[x,y] == 1) {
				self.color_buffer
			} {
				self.color_off
			}
		};
	},

	midinote_to_tile_index: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		pos = pos.x + (pos.y * 8);
		pos;
	},

	response_cc_on: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL");
		self.current_cc_on = midinote;
		res =switch(midinote,
			self.buttons[4], {
				if(self.state_edit == 1) {
					self.set_cc_led(self.buttons[4], \off);
					self.sceneset.state_edit = 0;
					//self;
					nil;
				} {

					self.set_cc_led(self.buttons[4], \yellow);
					self.sceneset.state_edit = 1;
					//self;
					nil;
				}
			},
			{

			}
		);
		if(res.notNil) { res.activate };
	},

	response_cc_off: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL off");
		self.current_cc_on = nil;
		res = switch(midinote,
			self.buttons[5], {
				"scene SEQ!!".debug;
				self.set_cc_led(self.buttons[5], \yellow);
				self.set_cc_led(self.buttons[6], \off);
				self.set_cc_led(self.buttons[7], \off);
				self.sceneset.seq;
			},
			self.buttons[6], {
				self.set_cc_led(self.buttons[5], \off);
				self.set_cc_led(self.buttons[6], \yellow);
				self.set_cc_led(self.buttons[7], \off);
				self.sceneset.subbuffer;
			},
			self.buttons[7], {
				self.set_cc_led(self.buttons[5], \off);
				self.set_cc_led(self.buttons[6], \off);
				self.set_cc_led(self.buttons[7], \yellow);
				self;
			},
			{

			}
		);
		if(res.notNil) { res.activate };
	},

	response_launcher_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var ledpos;
		var res;
		var tilex, tiley, tile;
		tilex = pos.y;
		tiley = self.active_row;
		tile = self.gridpos_to_tile_index(Point(tilex, tiley));
		[midinote, pos, tilex, tiley, tile, self.current_cc_on, self.state_edit].debug("[midinote, pos, tilex, tiley, tile, self.current_cc_on, self.state_edit]");
		if(self.current_cc_on == self.buttons[6]) {
			self.sceneset.subbuffer.set_active_row(pos.y);
			self.sceneset.subbuffer.activate;
		} {
			if(self.state_edit == 1) {
				~veco.vim_open_buffer(tile);
				tile.debug("response_on: tile");
			} {
				ledpos = Point(pos.y, self.active_row);
				res = ~veco.toggle_play_node_by_index(tile);
				[pos,ledpos,res].debug("res:::!!!! pos,ledpos,res");
				if(res == true) {
					self.state_play[ledpos.x,ledpos.y] = 1;
					self.set_led(midinote, self.state_to_color(ledpos));
				} {
					self.state_play[ledpos.x,ledpos.y] = 0;
					self.set_led(midinote, self.state_to_color(ledpos));
				};
			}
		}
	},

	response_on: { arg self, midinote;
		var tile = self.midinote_to_tile_index(midinote);
		var pos = self.midi_to_gridpos(midinote);
		var res;
		midinote.debug("tile: response_on: midinote");
		if(self.is_launcher_button(midinote)) {
			midinote.debug("tile: response_on: launcher!!!");
			self.sceneset.subbuffer.set_active_row(pos.y);
			self.sceneset.subbuffer.activate;
		} {

			if(self.state_edit == 1) {
				~veco.vim_open_buffer(tile);
				tile.debug("response_on: tile");
			} {
				res = ~veco.toggle_play_node_by_index(tile);
				res.debug("res:::!!!!");
				if(res == true) {
					self.state_play[pos.x,pos.y] = 1;
					self.set_led(midinote, self.state_to_color(pos));
				} {
					self.state_play[pos.x,pos.y] = 0;
					self.set_led(midinote, self.state_to_color(pos));
				};
			}
		};
	},

	pseqrow: { arg self, y;
		Pseq(8.collect { arg x;
			if(self.state[x, y] == self.color_on) {
				1
			} {
				\rest
			}
		})
	},

	response_off: { arg self;
		
	},
);

~launchpad_scene_subbuffer = (
	parent: ~launchpad_scene,
	color_on: \red,
	color_off: \off,
	color_play: \green,
	color_buffer: \dimred,
	color_buffer_play: 0x3d,

	state_subbuffers: { arg self; ~veco.get_subbuffer_presence(self.active_row) },
	state_buffers: { arg self; ~veco.get_buffer_presence },
	state_play: { arg self; self.sceneset.state_play },
	state_edit: { arg self; self.sceneset.state_edit },

	state_to_color: ~launchpad_scene_tile[\state_to_color],

	render_state: { arg self;
		//~veco.geupdate_buffer_presence;
		~veco.update_buffer_presence;
		self.state_subbuffers.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("launchpad_scene_subbuffer: render_state: subbuffer_state");
				self.state[x,y] = if(val == 1) { self.color_on } { self.color_off };
			}
		};
		self.state_buffers.rowAt(self.active_row).do { arg val, x;
			self.state[8, x] = self.state_to_color(Point(x, self.active_row));
		}
	},


	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var ledpos;
		var res;
		var tilex, tiley, tile;
		tilex = pos.y;
		tiley = self.active_row;
		tile = self.gridpos_to_tile_index(Point(tilex, tiley));
		self.state_edit.debug("launchpad_scene_subbuffer: response_on: state_edit. BODREL");
		if(self.is_launcher_button(midinote)) {

			self.sceneset.tile.response_launcher_on(midinote);
			//if(self.state_edit == 1) {
			//	~veco.vim_open_buffer(tile);
			//	tile.debug("response_on: tile");
			//} {
			//	ledpos = Point(pos.y, self.active_row);
			//	res = ~veco.toggle_play_node_by_index(tile);
			//	[pos,ledpos,res].debug("res:::!!!! pos,ledpos,res");
			//	if(res == true) {
			//		self.state_play[ledpos.x,ledpos.y] = 1;
			//		self.set_led(midinote, self.state_to_color(ledpos));
			//	} {
			//		self.state_play[ledpos.x,ledpos.y] = 0;
			//		self.set_led(midinote, self.state_to_color(ledpos));
			//	};
			//}
		} {
			if(self.state_edit == 1) {
				~veco.vim_open_subbuffer(tile, pos.x+1);
				[tile,pos].debug("response_on: open subbuffer: tile, pos");
			} {
				~veco.exec_subbuffer(tile, pos.x+1);
				pos.debug("response_on: exec subbuffer: pos");
			}
		}
	},

);

~launchpad_scene_seq_old = (
	parent: ~launchpad_scene,

	//state: Array2D.new(8,8),
	color_on: \red,
	color_off: \off,
	range_state: Array.fill(8, 0),
	ranges: [6,7],

	state_buffers: { arg self; ~veco.get_buffer_presence },

	seq_state: { arg self; self.sceneset.state_seq }, 
	reset_seq_state: { arg self; self.sceneset.state_seq = Array2D.new(8,8) },

	get_range_state: { arg self, index;
		self.range_state[index];
	},


	set_range_state: { arg self, index, val;
		self.range_state[index] = val;
	},

	render_state: { arg self;
		var active_row = self.active_row;
		self.seq_state.rowsDo { arg column, x;
			column.do { arg val, y;
				[x, y, val].debug("launchpad_scene_seq: render_state: seq_state");
				self.state[x,y] = if(val == 1) { self.color_on } { \off };
			}
		};
		self.state_buffers.rowAt(active_row).do { arg val, x;
			self.state[8, x] = self.sceneset.tile.state_to_color(Point(x, active_row));
		};
	},

	response_on: { arg self, midinote;
		var pos;
		var color;
		"resp: on".debug;
		pos = self.midi_to_gridpos(midinote);
		if(self.is_launcher_button(midinote)) {
			self.sceneset.tile.response_launcher_on(midinote);
		} {
			self.toggle_cell(midinote, pos);
		};
	},

	toggle_cell: { arg self, midi, pos;
		var color;
		color = self.color_on;
		if(self.seq_state[pos.x,pos.y] == 1) {
			self.seq_state[pos.x,pos.y] = 0;
			self.set_led(midi, \off);
		} {
			self.seq_state[pos.x,pos.y] = 1;
			self.set_led(midi, color);
		}
	},

	pseqrow: { arg self, y, repeat=1;
		Pseq(8.collect { arg x;
			if(self.seq_state[x, y] == 1) {
				false
			} {
				true
			}
		},repeat)
	},

	prythm: { arg self, y, repeat=inf;
		Pn(Plazy({
			self.pseqrow(y)
		}),repeat);
	},

	pryn: { arg self, name, repeat=inf;
		var idx = ~veco.name_to_index(name);
		[name, idx].debug("pryn");
		idx = idx % 8;
		Pn(Plazy({
			self.pseqrow(idx)
		}),repeat);
	},

	response_off: { arg self;
		
	},
);

//////////////////

~launchpad = (
	new: { arg self, controller;
		self = self.deepCopy;
	
		self.modifier_names = #[up, down, left, right, session, user1, user2, mixer];
		self.modifiers = ();
		debug("NEW class_launchpad");
		self.midiout = MIDIOut(0);

		self.init_midi_layout;
		self.controller = { controller };
		
		CmdPeriod.add({  self.sceneset.cleanup_function });
	
		self;
	},

	sceneset: { ~sceneset },

	state: { arg self;
		self.sceneset.state_leds;
	},

	buttons: [0,1,2,3,4,5,6,7] + 104,
	mode_buttons: [0,1,2,3,4,5,6,7] + 104,
	launcher_buttons: [0,1,2,3,4,5,6,7] * 16 + 8,

	is_launcher_button: { arg self, midinote;
		midinote % 16 == 8
	},

	colors: (
			red: 0x0f,
			red2: 0x0e,
			dimred: 0x0d,
			green: 0x3c,
			green2: 0x2c,
			dimgreen: 0x1c,
			yellow: 0x3e,
			orange: 0x2f,
			amber: 0x3f,
			off: 0,
	),

	init_midi_layout: { arg self;
		
		self.gridlayout = 9.collect { arg x;
			8.collect { arg y;
				//[x,y].debug("x,y");
				//x+(y*16).debug("res");
				x+(y*16)
			}
		};
		//self.gridlayout.debug("gridlayout");
		self.gridlayout;
	},

	color_to_velocity: { arg self, color=63;
		if(color.isNumber) {
			color
		} {
			self.colors[color] ?? 63
		}
		
	},

	gridpos_to_midi: { arg self, pos;
		var res = self.gridlayout[pos.x][pos.y];
		//res.debug("midipos");
		res;
	},

	midi_to_gridpos: { arg self, midi;
		var x, y;
		x = midi % 16;
		y = (midi / 16).asInteger;
		Point(x, y);
	},

	set_cc_led: { arg self, pos, color;
		if(pos < 50) {
			pos = pos + 104;
		};
		color = self.color_to_velocity(color);
		self.midiout.control(0, pos, color)
	},

	set_led: { arg self, pos, color;
		var midi;
		[pos,color].debug("set_led: pos, color");
		if(pos.class == Point) {
			midi = self.gridpos_to_midi(pos);
		} {
			midi = pos;
			pos = self.midi_to_gridpos(pos);
		};
		[midi, pos,color].debug("set_led: midi, pos, color 2");
		self.state[pos.x,pos.y] = color;
		[pos,color].debug("set_led: pos, color 3");
		color = self.color_to_velocity(color);
		[0,midi,color].debug("send noteOn");
		self.midiout.noteOn(0, midi, color);

	},
);

~launchpad_scene = (
	parent: ~launchpad,
	color_mode_on: \yellow,
	color_mode_off: \off,

	active_row: { arg self;
		self.sceneset.active_row
	},

	set_active_row: { arg self, idx;
		idx.debug("launchpad_scene_subbuffer: active_row");
		self.sceneset.active_row = idx;
	},

	get_node_by_index: { arg self, idx;
		self.controller.get_node_by_index(idx)
	},

	absolute_point_to_buf: { arg self, pos;
		var node;
		var res;
		node = self.controller.get_node_by_index(pos.x);
		if(node.notNil) {
			res = node.get_clip_by_index(pos.y, false);
		};
		res;
	},

	abspos_to_gridpos: { arg self, abspos, inrect=true;
		var pos = abspos - self.controller.clip_offset;
		var rect = Rect(0,0,8,8);
		[self.controller.clip_offset, abspos, pos, rect, rect.contains(pos)].debug("abspos_to_gridpos: offset, abs, pos, rect, contains");
		if(rect.contains(pos) or: {inrect != true}) {
			pos
		} {
			nil
		}
	},


	midi_to_offset_gridpos: { arg self, midi;
		self.midi_to_gridpos(midi) + self.controller.clip_offset;
	},

	offset_pos: { arg self, pos;
		pos + self.controller.clip_offset
	},

	launcher_pos_to_node: { arg self, pos;
		var nodeidx;
		nodeidx = pos.y + self.controller.clip_offset.x;
		self.controller.get_node_by_index(nodeidx);
	},

	install_midi_responder: { arg self;
		MIDIdef.noteOn(\launchpadOn, { arg vel, midinote;
			midinote.debug("midinote: noteOn responder");
			self.response_on(midinote);
		}).permanent_(true);
		MIDIdef.noteOff(\launchpadOff, { arg vel, midinote;
			midinote.debug("midinote: noteOFF responder");
			self.response_off(midinote);
		}).permanent_(true);
		MIDIdef.cc(\launchpadCC, { arg vel, midinote;
			if(vel == 127) {
				midinote.debug("midinote: ccOn responder");
				self.handle_modifiers_on(midinote);
				self.response_cc_on(midinote);
			} {
				midinote.debug("midinote: ccOff responder");
				self.handle_modifiers_off(midinote);
				self.response_cc_off(midinote);
			}
		}).permanent_(true);
	},

	update_display: { arg self;
		var midi;
		self.render_state;
		self.render_mode_state;
		//self.state.rowsDo { arg column, x;
		//	column.keep(9).do { arg color, y;
		//		//[color,x,y].debug("update_display: COLOR");
		//		if(color.isNil) {
		//			color = 0;
		//		};
		//		//[color,x,y].debug("update_display: COLOR2");
		//		color = self.color_to_velocity(color);
		//		midi = self.gridpos_to_midi(Point(x,y));
		//		//[midi,color].debug("update_display: midi, color");
		//		if(midi.notNil) {
		//			self.midiout.noteOn(0, midi, color);
		//		}
		//	}
		//};
		//self.sceneset.state_mode_leds.do { arg val, x;
		//	self.set_cc_led(x, val)
		//};
	},

	render_mode_state: { arg self;
		//self.sceneset.state_mode_leds.debug("render_mode_state");
		//self.sceneset.debug("render_mode_state");
		//self.sceneset.state_mode_leds[4] = if(self.sceneset.state_edit == 1) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[5] = if(self.sceneset.active_scene === self.sceneset.seq) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[6] = if(self.sceneset.active_scene === self.sceneset.subbuffer) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds[7] = if(self.sceneset.active_scene === self.sceneset.tile) { self.color_mode_on } { self.color_mode_off };
		//self.sceneset.state_mode_leds.debug("end render_mode_state");
		var on = switch(self.controller.mode,
			\copy, {
				0
			},
			\paste, {
				1
			},
			\clip, {
				4 
			},
			\seq, {
				5 
			},
			\edit, {
				6
			},
			\mixer, {
				7
			},
			{

			}
		);
		8.do { arg x;
			self.set_cc_led(x, if(x == on) { self.color_mode_on } { self.color_mode_off })
		}
	},

	clip_point: { arg self, point, rect;
		point = point.copy;
		point.x = point.x.clip(rect.left, rect.right);
		point.y = point.y.clip(rect.top, rect.bottom);
		point;
	},

	handle_modifiers_on: { arg self, midi;
		var idx = midi - 104;
		var modname = self.modifier_names[idx];
		self.modifiers[modname] = true;
	},

	handle_modifiers_off: { arg self, midi;
		var idx = midi - 104;
		var modname = self.modifier_names[idx];
		self.modifiers[modname] = false;
	},

	handle_grid_move: { arg self, midinote;
		var os = self.controller.clip_offset;
		var count = 1;
		var matrix = Rect(0,0,64,64);
		var rosae = (
			up: Point(0,-1),
			down: Point(0,1),
			left: Point(-1,0),
			right: Point(1,0),
		);
		var move = { arg dir;
			self.controller.clip_offset = self.clip_point(os + (rosae[dir]*count), matrix);
			self.render_state;
			nil;
		};
		if(self.modifiers[\session] == true) {
			count = 8;
		};
		switch(midinote,
			self.buttons[0], { // up
				move.(\up);
			},
			self.buttons[1], { // down
				move.(\down);
			},
			self.buttons[2], { // left
				move.(\left);
			},
			self.buttons[3], { // right
				move.(\right);
			}, {
				nil
			}
		);
	},

	activate: { arg self, update=true;
		"ACTIVATION".debug;
		if(self.sceneset.active_scene.notNil) {
			self.sceneset.active_scene.deactivate;
		};
		self.install_midi_responder;
		self.sceneset.active_scene = self;
		self.activate_hook;
		if(update) {
			self.update_display;
		}
	},

	deactivate: { arg self;
		// TODO
	},

	response_on: { arg self;
		
	},

	response_off: { arg self;
		
	},

	response_cc_on: { arg self, midi;
		self.sceneset.tile.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.tile.response_cc_off(midi);
	},

	gridpos_to_buf: { arg self, pos;
		var node;
		var res;
		pos = self.controller.clip_offset + pos;
		node = self.controller.get_node_by_index(pos.x);
		if(node.notNil) {
			res = node.get_clip_by_index(pos.y, false);
		};
		res;
	},

	gridpos_to_node_and_clip: { arg self, pos;
		var node;
		var clip;
		pos = self.controller.clip_offset + pos;
		node = self.controller.get_node_by_index(pos.x);
		if(node.notNil) {
			clip = node.get_clip_by_index(pos.y);
		};
		[node, clip];
	},
);

//////////////////

~launchpad_scene_clip = (
	parent: ~launchpad_scene,

	//state_to_color: ~launchpad_scene_tile[\state_to_color],

	render_state: { arg self;
		////~veco.geupdate_buffer_presence;
		//~veco.update_buffer_presence;
		//self.state_subbuffers.rowsDo { arg column, x;
		//	column.do { arg val, y;
		//		[x, y, val].debug("launchpad_scene_subbuffer: render_state: subbuffer_state");
		//		self.state[x,y] = if(val == 1) { self.color_on } { self.color_off };
		//	}
		//};
		//self.state_buffers.rowAt(self.active_row).do { arg val, x;
		//	self.state[8, x] = self.state_to_color(Point(x, self.active_row));
		//}
		"launchpad_scene_clip.render_state".debug;
		9.do { arg x;
			8.do { arg y;
				self.render_pos(Point(x,y));
			}
		};
	},

	render_column: { arg self, x;
		8.do { arg y; 
			self.render_pos(Point(x,y));
		}
	},

	render_line: { arg self, y;
		8.do { arg x; 
			self.render_pos(Point(x,y));
		}
	},

	color_active: \red,

	render_pos: { arg self, pos;
		var color_off = \off,
			color_present = \orange,
			color_active = self.color_active,
			color_playing = \green;
		var color;
		var node, buf;
		#node, buf = self.gridpos_to_node_and_clip(pos);
		if(buf.isNil) {
			color = color_off;
			[pos].debug("launchpad_scene_clip: render_pos: buf is nil");
		} {


			[buf.uname, buf.path, buf.is_present, buf.is_playing, buf.is_active].debug("launchpad_scene_clip: render_pos");

			color = if(buf.is_present == true) {
				if(buf.is_active == true) {
					if(node.is_playing == true) {
						color_playing;
					} {
						color_active;
					}
				} {
					color_present;
				}
			} {
				color_off;
			};

			self.set_led(pos, color);
		}

	},


	response_cc_on: { arg self, midinote;
		var res;
		var os = self.controller.clip_offset;
		midinote.debug("MIDINOTE CoNTROL ON");
		self.current_cc_on = midinote;
		//if([\clip, \edit, \mixer].includes(self.controller.mode)) {
		if(self.controller.mode == \clip) {
			self.handle_grid_move(midinote);
		};

		if(res.isNil) {
			res = switch(midinote,
				self.buttons[4], {
					//self.render_state;
					nil;
				},
				self.buttons[5], {
					self.controller.mode = \seq;
					self.sceneset.seq_chooser;
				},
				self.buttons[7], {
					self.controller.mode = \mixer_mod;
					self.sceneset.mixer_mod;
				},
				{
					nil;
				}
			);
		};
		if(res.notNil) { res.activate };
		self.render_mode_state;
	},

	response_cc_off: { arg self, midinote;
		var res;
		midinote.debug("MIDINOTE CoNTROL OFF");
		self.current_cc_on = midinote;
		res = switch(midinote,
			self.buttons[4], {
				self.controller.mode = \clip;
				self.sceneset.clip;
			},
			self.buttons[6], {
				self.controller.mode = \edit;
				self.sceneset.edit;
			},
			self.buttons[7], { 
				self.controller.mode = \mixer;
				self.sceneset.mixer;
			},
			{
				nil;
			}
		);
		if(res.notNil) { res.activate };
		self.render_mode_state;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		var opos;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {
			var y;
			midinote.debug("launchpad_scene_clip.response_on: is a launcher");
			opos = self.controller.clip_offset + pos;
			y = opos.y;
			64.do { arg x;
				node = self.controller.get_node_by_index(x);
				if(node.notNil) {
					clip = node.get_clip_by_index(y);
				};
				if(clip.notNil and: {clip.is_present}) {
					clip.response_on;
				}
			};
			self.render_state;

		} {
			midinote.debug("launchpad_scene_clip.response_on: go!");
			if(self.modifiers[\session] == true) {
				self.controller.clip_offset = pos * 8;
			} {
				#node, clip = self.gridpos_to_node_and_clip(pos);
				if(clip.is_present) {
					clip.response_on;
				} {
					node.stop_node(true, {
						self.render_column(pos.x);
					});
				};
				//self.render_column(pos.x);
			};
			self.render_state;
		}
	},

);

~launchpad_scene_seq_chooser = (
	parent: ~launchpad_scene_clip,
	not_default_mode: false,

	response_cc_off: { arg self, midinote;
		var res;
		self.not_default_mode.debug("launchpad_scene_seq_chooser.response_cc_off");
		if(self.not_default_mode != true) {
			self.controller.set_seq_active_mode;
		};
		self.not_default_mode = false;

		self.controller.mode = \seq;
		res = self.sceneset.seq;
		if(res.notNil) { res.activate };
		// TODO: indicate mode of seq with arrow led
		//self.render_mode_state;
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_seq_chooser.response_on");

		pos = self.offset_pos(pos);

		if(self.is_launcher_button(midinote)) {
			self.controller.set_seq_row_mode(pos.y)
		} {
			self.controller.set_seq_column_mode(pos.x)
		};
		self.not_default_mode = true;
	},
);

~launchpad_scene_seq = (
	parent: ~launchpad_scene,
	color_on: \red,
	color_off: \off,
	color_disabled: \dimred,
	color_end: \dimgreen,

	seq_buffers: { arg self, x;
		if(x.isNil) {
			8.collect { arg x;
				var point = self.controller.active_seq_points[x];
				self.absolute_point_to_buf(point);
			}
		} {
			var point = self.controller.active_seq_points[x];
			self.absolute_point_to_buf(point);
		}
	},

	deactivate: { arg self;
		self.seq_buffers.do { arg buf; buf.save_data; }
	},

	render_state: { arg self;
		debug("launchpad_scene_seq.render_state");
		8.do { arg x;
			self.render_line(x)
		}
	},

	activate_hook: { arg self;
		self.controller.seq_offset = 0;
	},

	render_line: { arg self, y;
		var os;
		var seq;
		//os = self.controller.seq_offset;
		//seq = self.seq_buffers(y).stepseq.drop(os).keep(8);
		8.do { arg x;
			self.render_pos(Point(x, y));
		}

	},

	render_pos: { arg self, pos;
		//var val;
		//var color;
		//var buf;
		//self.seq_buffers(pos.y).debug("render_pos: seq_buffers");
		//buf = self.seq_buffers(pos.y);
		//val = buf.stepseq[pos.x];
		//color = if(buf.is_present) { self.color_on } { self.color_disabled };
		//self.set_led(pos, if(val == 1) { color } { self.color_off })
		var x = pos.x, y = pos.y;
		var val = self.get_relpos_seq(y, x);
		var color;
		var color_on;
		color_on = if(self.seq_buffers(y).is_present.not) {
			self.color_disabled;
		} {
			self.color_on;
		};
		color = switch(val,
			nil, {
				self.color_end;
			},
			1, {
				color_on;
			},
			0, {
				self.color_off;
			}
		);
		self.set_led(Point(x,y), color);
	},

	handle_grid_move: { arg self, midinote;
		var os = self.controller.seq_offset;
		var count = 1;
		var rosae = (
			left: -1,
			right: 1,
		);
		var move = { arg dir;
			self.controller.seq_offset = (os + (rosae[dir]*8)).clip(0,64);
			[self.controller.seq_offset,os, dir, rosae[dir]].debug("seqos,os");
			self.render_state;
			nil;
		};
		switch(midinote,
			self.buttons[0], { // up
			},
			self.buttons[1], { // down
			},
			self.buttons[2], { // left
				move.(\left);
			},
			self.buttons[3], { // right
				move.(\right);
			}, {
				nil
			}
		);
		self.controller.seq_offset.debug("seq_offset");
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	set_relpos_seq: { arg self, y, idx, val;
		var seq = self.seq_buffers(y).stepseq;
		idx = idx + self.controller.seq_offset;
		if(seq[idx].notNil) {
			seq[idx] = val;
		};
	},

	toggle_relpos_seq: { arg self, y, idx;
		var seq = self.seq_buffers(y).stepseq;
		idx = idx + self.controller.seq_offset;
		if(seq[idx].notNil) {
			seq[idx] = if(seq[idx] == 1) { 0 } { 1 };
		};
	},
	
	get_relpos_seq: { arg self, y, idx;
		var seq = self.seq_buffers(y).stepseq;
		idx = idx + self.controller.seq_offset;
		if(seq[idx].notNil) {
			seq[idx];
		};
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		var seq;
		var idx;
		midinote.debug("launchpad_scene_seq.response_on");

		if(self.is_launcher_button(midinote)) {
			clip = self.seq_buffers(pos.y);
			clip.response_on;
		} {
			self.toggle_relpos_seq(pos.y, pos.x);
			self.render_pos(pos);
		}
	},

);

~launchpad_scene_edit = (
	parent: ~launchpad_scene,

	render_state: { arg self;
		self.sceneset.clip.render_state;
	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		if(self.controller.mode == \edit) {
			self.handle_grid_move(midi);
		};
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_clip.response_on");

		if(self.is_launcher_button(midinote)) {
			node = self.launcher_pos_to_node(pos);
			if(node.notNil) {
				node.open_in_vim;
			};
			//self.render_pos(pos);
		} {
			midinote.debug("launchpad_scene_clip.response_on: go!");
			#node, clip = self.gridpos_to_node_and_clip(pos);
			if(clip.notNil) {
				clip.open_in_vim;
			};
			self.render_pos(pos);
		}
	},

);

~launchpad_scene_mixer = (
	parent: ~launchpad_scene,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	color_edit: \yellow,

	render_state: { arg self;
		8.do { arg x; self.render_column(x) }
	},

	render_column: { arg self, x;
		var node;
		var ox;
		var off_column = { arg x;
			8.do { arg y;
				self.set_led(Point(x, y), if(y == 3) { self.color_edit } { \off });
			};
		};
		ox = self.controller.clip_offset.x + x;
		node = self.controller.get_node_by_index(ox);
		if(node.notNil) {

			if(node.is_present) {
				if(node.is_playing) {
					self.set_led(Point(x, 0), \green);
				} {
					self.set_led(Point(x, 0), \red);
				};
				self.set_led(Point(x, 1), if(node.is_muted) { \dimred } { \dimgreen });
				self.set_led(Point(x, 2), \orange);
				self.set_led(Point(x, 3), self.color_edit);
				self.set_led(Point(x, 4), \dimred);
				3.do { arg y;
					self.set_led(Point(x, y+5), \off);
				};
			} {
				off_column.(x);
			};
		} {
			//[x, ox].debug("launchpad_scene_mixer.render_column: node is nil, x, ox");
			off_column.(x);
		};


	},

	response_cc_on: { arg self, midi;
		//self.render_state;
		if(self.controller.mode == \mixer) {
			self.handle_grid_move(midi);
		};
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			midinote.debug("launchpad_scene_mixer.response_on: go!");
			opos = self.offset_pos(pos);
			node = self.get_node_by_index(opos.x);
			switch(pos.y,
				0, {
					node.toggle_play;
				},
				1, {
					node.mute_node;
				},
				2, {
					node.execute;
				},
				3, {
					node.open_in_vim;
				},
				4, {
					node.fadeout;
				}
			);
			self.render_column(pos.x);
		}
	},

	response_off: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mixer.response_on");

		if(self.is_launcher_button(midinote)) {

		} {
			midinote.debug("launchpad_scene_mixer.response_on: go!");
			opos = self.offset_pos(pos);
			node = self.get_node_by_index(opos.x);
			switch(pos.y,
				1, {
					node.unmute_node;
				},
			);
			self.render_column(pos.x);
		}
	},

);

~launchpad_scene_mixer_mod = (
	parent: ~launchpad_scene,

	// ** launcher layout
	// - copy
	// - paste
	// - delete

	render_state: { arg self;
		//self.sceneset.clip.render_state;
	},


	response_cc_on: { arg self, midi;
	},

	response_cc_off: { arg self, midi;
		self.sceneset.clip.response_cc_off(midi);
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		var res;
		midinote.debug("launchpad_scene_mixer_mod.response_on");

		if(self.is_launcher_button(midinote)) {
			res = switch(pos.y,
				0, { // copy
					self.controller.mode = \copy;
					self.sceneset.mode_copy;
				},
				1, { // paste
					self.controller.mode = \paste;
					self.sceneset.mode_paste;
				},
				{
					nil
				}
			
			);
			if(res.notNil) { 
				res.skip_first_time = true;
				res.activate;
			};
		} {
		}
	},

);

~launchpad_scene_mode_copy = (
	parent: ~launchpad_scene,
	color_clipboard: \red,

	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	render_state: { arg self;
		self.sceneset.clip.render_state;
		self.render_clipboard;
	},

	render_clipboard: { arg self;
		var pos, abspos, clip;
		switch(self.controller.clipboard_mode,
			\row, {
				clip = self.controller.clipboard_clip[0];
				pos = self.abspos_to_gridpos(clip.get_abspos, false);
				if(pos.y.inclusivelyBetween(0,7)) {
					//self.blink_led(self.launcher_buttons[pos.y], \red, \green);
					self.set_led(self.launcher_buttons[pos.y], self.color_clipboard);
				}
			},
			\column, {

			},
			\active, {
				//self.controller.clipboard_clip.do { arg clip_source, x;
				//	[clip_source.isNil, x].debug("launchpad_scene_mode_copy.render_clipboard: active, clipsourceisnil, x");
				//	if(clip_source.notNil) {
				//		abspos = clip_source.get_abspos;
				//		pos = self.abspos_to_gridpos(abspos);
				//		if(pos.notNil) {
				//			self.set_led(pos, self.color_clipboard);
				//		}
				//	}
				//};
			},
			\clip, {
				clip = self.controller.clipboard_clip[0];
				if(clip.notNil) {
					pos = self.abspos_to_gridpos(clip.get_abspos, true);
					if(pos.notNil) {
						//self.blink_led(pos, \red, \green);
						self.set_led(pos, self.color_clipboard);
					}
				}
			},
		);
	},


	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.unblink_led;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	activate_hook: { arg self;
		var node, clip;
		self.controller.clipboard_mode = \active;
		self.controller.clipboard_clip = self.controller.msize.x.collect { arg x;
			node = self.get_node_by_index(x);
			if(node.notNil and: { node.is_playing == true }) {
				clip = node.get_clip_by_index(node.active_clip_index);
			} {
				nil;
			}
		};
	},

	response_cc_off: { arg self, midi;
		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mode_copy.response_on");

		if(self.is_launcher_button(midinote)) {
			var y = pos.y + self.controller.clip_offset.y;
			self.controller.clipboard_clip = 64.collect { arg x;
				clip = self.absolute_point_to_buf(Point(x, y));
			};
			self.controller.clipboard_clip.debug("launchpad_scene_mode_copy.response_on: clipboard_clip");
			self.controller.clipboard_mode = \row;
			self.render_state;
		} {
			midinote.debug("launchpad_scene_mode_copy.response_on: go!");
			clip = self.gridpos_to_buf(pos);
			self.controller.clipboard_clip = [clip];
			self.controller.clipboard_mode = \clip;
			self.render_state;
		}
	},

);

~launchpad_scene_mode_paste = (
	parent: ~launchpad_scene_mode_copy,

	color_pasted: \yellow,
	// ** layout
	// - start/stop
	// - mute/unmute
	// - exec node buffer
	// - edit node buffer

	render_state: { arg self;
		self.sceneset.clip.render_state;
		self.render_clipboard;
	},

	render_clipboard: { arg self;
		self.sceneset.mode_copy.render_clipboard;
	},

	activate_hook: { arg self;
	},


	response_cc_on: { arg self, midi;
		//self.render_state;
		//self.sceneset.mode_copy.unblink_led;
		self.handle_grid_move(midi);
		self.sceneset.clip.response_cc_on(midi);
	},

	response_cc_off: { arg self, midi;
		if(self.skip_first_time != false) {
			self.skip_first_time = false;
		} {
			self.sceneset.clip.response_cc_off(midi);
		}
	},

	response_on: { arg self, midinote;
		var pos = self.midi_to_gridpos(midinote);
		var opos;
		var buf;
		var node, clip;
		midinote.debug("launchpad_scene_mode_copy.response_on");

		if(self.is_launcher_button(midinote)) {
			var y = pos.y + self.controller.clip_offset.y;
			var clip_source, clip_dest;
			var dest_abspos, dest_gridpos;
			if(self.controller.clipboard_mode == \row) {
				64.do { arg x;
					dest_abspos = Point(x, y);
					dest_gridpos = self.abspos_to_gridpos(dest_abspos);
					clip_source = self.controller.clipboard_clip[x];
					if(clip_source.notNil) {

						clip_dest = self.absolute_point_to_buf(dest_abspos);
						if(clip_source.is_present and: { clip_dest.is_present.not }) {
							clip_dest.load_data_from_clip(clip_source);
							if(dest_gridpos.notNil) {
								self.set_led(dest_gridpos, self.color_pasted);
							};
						}
					}
				};
			} {
				if(self.controller.clipboard_mode == \active) {

					self.controller.clipboard_clip.do { arg clip_source, x;
						if(clip_source.notNil) {

							dest_abspos = Point(x, y);
							dest_gridpos = self.abspos_to_gridpos(dest_abspos);
							clip_dest = self.absolute_point_to_buf(dest_abspos);
							if(clip_source.is_present  and: {  clip_dest.is_present.not }) {
								clip_dest.load_data_from_clip(clip_source);
								if(dest_gridpos.notNil) {
									self.set_led(dest_gridpos, self.color_pasted);
								};
							}
						}
					};
				}
			}
		} {
			var clip_source, clip_dest;
			clip_source = self.controller.clipboard_clip[0];
			clip_dest = self.gridpos_to_buf(pos);
			if(clip_source.is_present and: { clip_dest.is_present.not }) {
				clip_dest.load_data_from_clip(clip_source);
				self.set_led(pos, self.color_pasted);
			}
		}
	},

);


/////////// TODO
// - navigation with session
//	- move by 8 with session + arrow
//	- move to page with session + pad
// - navigation in seq
// - use launcher in seq
// - implement muting

	//unblink_led: { arg self;
	//	if(self.blinker.notNil) {
	//		//debug("MAIS STOP BORDEL=============================");
	//		self.blinker.stop;
	//		switch(self.controller.clipboard_mode,
	//			\clip, {
	//				self.sceneset.clip.render_pos(self.blinker_pos);
	//			},
	//			{
	//				self.set_led(self.blinker_pos, \off);
	//			}
	//		);
	//	} {
	//		//debug("JUSTE PAS POSSIBLE -----------------------------");

	//	};
	//},

	//blink_led: { arg self, pos, color1, color2, time=0.5;
	//	self.unblink_led;
	//	self.blinker_pos = pos;
	//	self.blinker = Task({
	//		10.do {
	//			self.set_led(pos, color1);
	//			time.wait;
	//			self.set_led(pos, color2);
	//			time.wait;
	//		};
	//	});
	//	self.blinker.play;
	//},