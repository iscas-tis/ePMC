(*
 * get_unsat_core_ids.ml: Helper for MathSAT 4, to compute unsatisfiable cores
 * with an external boolean-unsat-core extractor. Given a "bool+tlemmas"
 * and a "bool only" unsat core file, it returns the ids of the clauses
 * appearing in the unsat core
 *
 * to compile it (with findlib):
 * ocamlfind opt -linkpkg -package "str" get_unsat_core_ids.ml
 *               -o get_unsat_core_ids 
 *
 * author: Alberto Griggio <alberto.griggio@disi.unitn.it>
 *)

type clause = int list

module OrdClause =
struct
  type t = clause
  let compare = Pervasives.compare
end
module ClauseMap = Map.Make(OrdClause)
  

let get_clauses chan =
  let theory_clauses = ref [] in
  let orig_clauses = ref [] in
  let rec process line cls_id is_theory =
    if Str.string_match (Str.regexp "^c theory clause follows") line 0 then
      process (input_line chan) cls_id true
    else if Str.string_match (Str.regexp "^c \\([0-9]+\\)") line 0 then
      let new_cls_id = (int_of_string (Str.matched_group 1 line)) in
      process (input_line chan) new_cls_id is_theory
    else
      let tokens = Str.split (Str.regexp "[ \t]+") line in
      if (List.length tokens) = 0 or (List.hd tokens) = "c"
        or (List.hd tokens) = "p" then
          process (input_line chan) cls_id false
      else (
        let c =
          (List.sort compare (List.tl (List.rev_map int_of_string tokens))) in
        if is_theory then theory_clauses := (cls_id, c)::!theory_clauses
        else orig_clauses := (cls_id, c)::!orig_clauses;
        process (input_line chan) cls_id false
      )
  in
  try
    process (input_line chan) 0 false
  with End_of_file ->
    let f = (fun s c -> ClauseMap.add (snd c) (fst c) s) in
    (List.fold_left f ClauseMap.empty !theory_clauses,
     List.fold_left f ClauseMap.empty !orig_clauses)
;;


let get_ids chan orig_clauses theory_clauses =
  let orig_ids = ref [] in
  let theory_ids = ref [] in
  let id c =
    try (false, ClauseMap.find c orig_clauses)
    with Not_found -> (true, ClauseMap.find c theory_clauses)
  in
  let rec process line =
    if Str.string_match (Str.regexp "^ *\\(c\\|p\\)") line 0 then
      process (input_line chan)
    else
      let tokens = Str.split (Str.regexp "[ \t]+") line in
      if (List.length tokens) = 0 then
        process (input_line chan)
      else
        let c =
          (List.sort compare (List.tl (List.rev_map int_of_string tokens))) in
        let is_theory, cls_id = id c in
        if is_theory then theory_ids := cls_id::!theory_ids
        else orig_ids := cls_id::!orig_ids;
        process (input_line chan)
  in
  try
    process (input_line chan)
  with End_of_file ->
    (List.sort compare !orig_ids), (List.sort compare !theory_ids)
;;



let get_unsat_core_ids smt_core unsat_core =
  try
    let smt_core_chan = open_in smt_core in
    let theory_clauses, orig_clauses = get_clauses smt_core_chan in
    let sat_core_chan = open_in unsat_core in
    let orig_ids, theory_ids =
      get_ids sat_core_chan orig_clauses theory_clauses
    in
    let f = (fun i -> Printf.printf "%d\n" i) in
    print_string "c original clauses ids:\n";
    List.iter f orig_ids;
    print_string "c theory clauses ids:\n";
    List.iter f theory_ids;
  with _ ->
    print_string "ERROR!\n";
    exit 1
;;
  


let main () =
  let me = Filename.basename Sys.argv.(0) in
  if Array.length Sys.argv <> 3 then (
    Printf.printf "Usage: %s <smt_core.cnf> <unsat_core.cnf>\n" me;
    exit 1
  );
  ignore(get_unsat_core_ids Sys.argv.(1) Sys.argv.(2))
in
main ()
